package swtGrocery.backend.services.Alghorithms;

import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import swtGrocery.backend.entities.PastPurchaseItem;
import swtGrocery.backend.repositories.PastPurchaseItemRepository;

/**
 * The AprioriAlgorithm class implements the Apriori algorithm for association rule mining
 * based on past purchase data. It discovers frequent itemsets and generates association rules
 * based on minimum support and confidence thresholds.
 */
public class AprioriAlgorithm {

  // Minimum support and confidence thresholds
  private static final double MIN_SUPPORT_THRESHOLD = 0.03;
  private static final double MIN_CONFIDENCE_THRESHOLD = 0.6;

  /**
   * Runs the Apriori algorithm on the provided past purchase items to discover frequent itemsets.
   *
   * @param pastPurchaseItems List of PastPurchaseItem objects representing past purchases
   *
   * @return Map containing frequent itemsets and their support counts
   */
  public Map<Object, Object> runAprioriAlgorithm(
    List<PastPurchaseItem> pastPurchaseItems
  ) {
    // Initialization of the Apriori library and setting a placeholder for the last frequency
    Map<Object, Object> aprioriLibrary = new HashMap<>();
    aprioriLibrary.put("lastFrequency", "none");

    // Building a mapping of transaction dates to item lists
    Map<String, List<String>> transactionMapping = buildTransactionMapping(
      pastPurchaseItems
    );

    // Calculating the minimum support count based on the threshold
    int minSupport = (int) Math.ceil(
      MIN_SUPPORT_THRESHOLD * transactionMapping.size()
    );

    // Counting the frequency of individual items in the transactions
    Map<String, Integer> itemFrequencyMap = countItemFrequency(
      transactionMapping
    );

    // Finding and storing frequent items with their support counts
    List<String> frequentItems = findFrequentItems(
      minSupport,
      itemFrequencyMap,
      aprioriLibrary
    );

    // If no frequent item, the algorithm stops!
    if (frequentItems.isEmpty()) {
      return aprioriLibrary;
    }

    // Now we will perform a loop to find all frequent itemsets of size > 1
    // starting from size k = 2.
    // The loop will stop when no candidates can be generated.
    List<List<String>> level = null;
    int k = 2;
    do {
      // Generate candidates of size K
      List<List<String>> candidateSets = null;

      if (k == 2) {
        // For the first iteration, generate pairwise combinations
        candidateSets = generatePairwiseCombinations(frequentItems);
      } else {
        // Otherwise, use the regular way to generate candidates
        candidateSets = generateKSizeCombinations(level, k);
      }

      // We scan the database one time to calculate the support
      // of each candidate and keep those with higher support.
      Map<List<String>, Integer> itemSetFrequencyMap = countItemSetFrequency(
        transactionMapping,
        candidateSets
      );

      // Filtering out the candidates below the minimum support
      level = new ArrayList<List<String>>();
      for (Map.Entry<List<String>, Integer> entry : itemSetFrequencyMap.entrySet()) {
        if (entry.getValue() >= minSupport) {
          level.add(entry.getKey());
          aprioriLibrary.put(entry.getKey(), entry.getValue());
        }
      }

      if (!level.isEmpty()) {
        aprioriLibrary.put("lastFrequency", level);
      }

      k++;
    } while (level.isEmpty() == false);

    return aprioriLibrary;
  }

  /**
   * Counts the frequency of each itemset in the transaction mapping.
   *
   * @param transactionMapping Mapping of transaction dates to item lists
   * @param candidateSets List of candidate itemsets
   *
   * @return Map containing itemsets and their frequency counts
   */
  private Map<List<String>, Integer> countItemSetFrequency(
    Map<String, List<String>> transactionMapping,
    List<List<String>> candidateSets
  ) {
    Map<List<String>, Integer> itemSetFrequencyMap = new HashMap<>();
    for (List<String> itemList : transactionMapping.values()) {
      for (List<String> candidateSet : candidateSets) {
        if (itemList.containsAll(candidateSet)) {
          Collections.sort(candidateSet);
          itemSetFrequencyMap.merge(candidateSet, 1, Integer::sum);
        }
      }
    }
    return itemSetFrequencyMap;
  }

  /**
   * Identifies and returns frequent items based on the minimum support threshold.
   *
   * @param minSupport Minimum support threshold
   * @param itemFrequencyMap Map containing item frequencies
   * @param aprioriLibrary Map to store the discovered frequent itemsets and their support counts
   *
   * @return List of frequent item names
   */
  private List<String> findFrequentItems(
    int minSupport,
    Map<String, Integer> itemFrequencyMap,
    Map<Object, Object> aprioriLibrary
  ) {
    List<String> frequentItems = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : itemFrequencyMap.entrySet()) {
      if (entry.getValue() >= minSupport) {
        frequentItems.add(entry.getKey());
        List<String> arrayList = new ArrayList<>();
        arrayList.add(entry.getKey());
        aprioriLibrary.put(arrayList, entry.getValue());
      }
    }
    return frequentItems;
  }

  /**
   * Counts the frequency of each individual item in the transaction mapping.
   *
   * @param transactionMapping Mapping of transaction dates to item lists
   *
   * @return Map containing item names and their frequency counts
   */
  private Map<String, Integer> countItemFrequency(
    Map<String, List<String>> transactionMapping
  ) {
    Map<String, Integer> itemFrequencyMap = new HashMap<>();
    for (List<String> itemList : transactionMapping.values()) {
      for (String item : itemList) {
        itemFrequencyMap.merge(item, 1, Integer::sum);
      }
    }
    return itemFrequencyMap;
  }

  /**
   * Builds a mapping of transaction dates to item lists from the provided past purchase items.
   *
   * @param pastPurchaseItems List of PastPurchaseItem objects representing past purchases
   *
   * @return Map containing transaction dates and corresponding item lists
   */
  private Map<String, List<String>> buildTransactionMapping(
    List<PastPurchaseItem> pastPurchaseItems
  ) {
    Map<String, List<String>> transactionMapping = new HashMap<>();
    for (PastPurchaseItem item : pastPurchaseItems) {
      String date = item.getItemPurchaseDate().toString();
      transactionMapping
        .computeIfAbsent(date, key -> new ArrayList<>())
        .add(item.getItemName());
    }
    return transactionMapping;
  }

  /**
   * Creates association rules based on the discovered frequent itemsets and input items.
   *
   * @param aprioriLibrary Map containing frequent itemsets and their support counts
   * @param items List of input items
   *
   * @return Set of suggested items based on association rules
   */
  public Set<String> createAssociationRules(
    Map<Object, Object> aprioriLibrary,
    List<String> items
  ) {
    Set<String> suggestions = new HashSet<>();

    if (aprioriLibrary.get("lastFrequency") == "none") {
      return suggestions;
    }
    Collections.sort(items);
    List<List<String>> lastFrequencies = (List<List<String>>) aprioriLibrary.get(
      "lastFrequency"
    );

    for (List<String> frequency : lastFrequencies) {
      if (!frequency.containsAll(items)) {
        continue;
      }
      List<String> remainingItems = new ArrayList<>(frequency);
      remainingItems.removeAll(items);

      Integer frequencyCount = (Integer) aprioriLibrary.get(frequency);
      Integer remainingItemsCount = (Integer) aprioriLibrary.get(
        remainingItems
      );

      if (
        remainingItemsCount != null &&
        frequencyCount != null &&
        ((double) frequencyCount / remainingItemsCount) >=
        MIN_CONFIDENCE_THRESHOLD
      ) {
        suggestions.addAll(remainingItems);
      }
    }

    return suggestions;
  }

  /**
   * Generates combinations of size K from the given levels of itemsets.
   *
   * @param levels List of itemsets of varying sizes
   * @param subsetLength Size of the combinations to generate
   *
   * @return List of combinations of size K
   */
  private List<List<String>> generateKSizeCombinations(
    List<List<String>> levels,
    int subsetLength
  ) {
    // Create a Set to store unique items
    Set<String> uniqueItems = new HashSet<>();
    for (List<String> level : levels) {
      addArrayItemsToSet(level, uniqueItems);
    }

    // Find all subsets of the specified length from the set of unique items
    List<List<String>> subsets = findSubsets(uniqueItems, subsetLength);

    return subsets;
  }

  /**
   * Finds all subsets of the given length from the provided set of unique items.
   *
   * @param uniqueItems Set of unique items
   * @param subsetLength Length of subsets to find
   *
   * @return List of subsets of the specified length
   */
  private List<List<String>> findSubsets(
    Set<String> uniqueItems,
    int subsetLength
  ) {
    List<List<String>> result = new ArrayList<>();
    generateSubsets(
      uniqueItems.toArray(new String[0]),
      subsetLength,
      0,
      new ArrayList<>(),
      result
    );
    return result;
  }

  /**
   * Recursively generates all subsets of the specified length from the given array of items.
   *
   * @param items Array of items
   * @param subsetLength Length of subsets to generate
   * @param start Starting index for subset generation
   * @param currentSubset Current subset being constructed
   * @param result List to store generated subsets
   */
  private void generateSubsets(
    String[] items,
    int subsetLength,
    int start,
    List<String> currentSubset,
    List<List<String>> result
  ) {
    if (currentSubset.size() == subsetLength) {
      result.add(new ArrayList<>(currentSubset));
      return;
    }

    for (int i = start; i < items.length; i++) {
      currentSubset.add(items[i]);
      generateSubsets(items, subsetLength, i + 1, currentSubset, result);
      currentSubset.remove(currentSubset.size() - 1);
    }
  }

  /**
   * Adds items from the provided array to the given set of unique items.
   *
   * @param array List of items to add to the set
   * @param uniqueItems Set of unique items
   */
  private void addArrayItemsToSet(List<String> array, Set<String> uniqueItems) {
    for (String item : array) {
      uniqueItems.add(item);
    }
  }

  /**
   * Generates pairwise combinations from the list of frequent items.
   *
   * @param frequent List of frequent items
   *
   * @return List of pairwise combinations
   */
  private List<List<String>> generatePairwiseCombinations(
    List<String> frequent
  ) {
    List<List<String>> candidates = new ArrayList<List<String>>();

    for (int i = 0; i < frequent.size(); i++) {
      String item1 = frequent.get(i);
      for (int j = i + 1; j < frequent.size(); j++) {
        String item2 = frequent.get(j);

        List<String> itemPair = new ArrayList<>();
        itemPair.add(item1);
        itemPair.add(item2);
        candidates.add(itemPair);
      }
    }
    return candidates;
  }
}
