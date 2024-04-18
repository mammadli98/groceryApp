package swtGrocery.backend;

public class Utilities {

  /**
   * Modify input item with just Capital letter
   *
   * @param name the name of the item to modify
   *
   * @return Modified item String
   */
  public static String capitalizeFirstLetter(String name) {
    if (name == null || name.isEmpty()) {
      return name; // Return unchanged if input is null or empty
    }

    String[] wordSplit = name.split("\\s+");

    for (int i = 0; i < wordSplit.length; i++) {
      wordSplit[i] =
        wordSplit[i].substring(0, 1).toUpperCase() +
        wordSplit[i].substring(1).toLowerCase();
    }

    StringBuilder capitalizedItemName = new StringBuilder();
    for (int i = 0; i < wordSplit.length; i++) {
      capitalizedItemName.append(wordSplit[i]);
      if (i < wordSplit.length - 1) { // this if statement is to not add a space at the end of the string
        capitalizedItemName.append(" ");
      }
    }
    return capitalizedItemName.toString(); // Convert to lowercase and capitalize first letter
  }
}
