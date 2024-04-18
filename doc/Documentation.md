# swtGrocery Documentation

## Repository structure

The below overview contains the most relevant files and folders, each accompanied by a brief description of its use.

```plain
.
├── doc
│   ├── Documentation.md %% information on development tools and the implemented software (this document)
│   └── Specification.md %% a textual description of what the software shall do
├── docker-compose.yml %% database service configuration
├── infrastructure %% further database configuration
├── LICENSE %% GNU GPL v3 software license
├── README.md %% a brief summary of this repository
├── .gitignore %% a list of patterns indicating files to be ignored by git
└── swtGrocery %%
    └── ... %% source code of the swtGrocery software
```

## Required software

**Operating system**  
The software should be runnable on all standard operating systems that provide installation candidates for the below
mentioned development tools. The software has in particular been tested on Ubuntu 18.04 LTS, on Windows 10, and on
Windows 11.

**Required deployment tools**

- git, see https://git-scm.com/
- Java 11 OpenJDK, see https://jdk.java.net/java-se-ri/11
- Docker, see https://www.docker.com/ (might need to install docker-compose separately)

**Further recommended tools for development**

- IntelliJ IDEA, see https://www.jetbrains.com/de-de/idea/
- Scene Builder, see https://gluonhq.com/products/scene-builder/#download
- node.js, see https://nodejs.org/en/download/
- DBeaver, see https://dbeaver.io/

## Deployment

1. Install the software mentioned above
2. Create a new ssh key and add it to your gitlab account (see https://gitlab.rz.uni-bamberg.de/-/profile/keys)
3. Clone this repository
4. Start the database via `docker-compose up database` in the commandline in the root directory of this repository.
5. Open a new commandline, Navigate to the `swtGrocery` directory, and start the swtGrocery application
   via `./gradlew bootRun`

Afterwards, stop the database using `docker-compose down`. In order to remove any data currently stored in the database
use `docker-compose down -v`.

## Project structure

The below overview contains the most relevant files and folders, each accompanied by a brief description of its use.

```plain
swtGrocery
├── build.gradle %% the gradle build file
├── gradlew %% the gradle wrapper script
├── src
    ├── main
    │   ├── java
    │   │   └── swtGrocery
    │   │       ├── Main.java %% starts the application
    │   │       ├── App.java %% defines the SpringBootApplication
    │   │       ├── backend %% all resources associated with the backend component
    │   │       │   ├── entity %% represent concepts persisted in the database
    │   │       │   ├── repository %% provides basic CRUD operations on entities, e.g., find, save, delete, etc.
    │   │       │   ├── service %% implement the business logic and persist changes made to entities in the database using repositories
    │   │       │   └── controller %% backend controllers receive DTOs, invoke services, and return DTOs
    │   │       ├── frontend
    │   │       │   └── controller %% frontend controllers map user interaction, e.g., clicking on a button, to backend controller actions, e.g., assigning a task
    │   │       └── shared %% DTO classes shared between the frontend and backend
    │   └── resources %% FXML view files and configuration of the application for deployment
    └── test
        ├── java %% unit and integration tests
        └── resources %% test data and configuration of the application for testing
```

## Architecture & Design

The swtGrocery application comprises a frontend and a backend component. It relies on an existing mariaDB database. The
backend uses Spring Boot, an open-source framework for developing Java applications, and the frontend uses JavaFX, an
open-source library for building a UI using an XML-based language.

**api and backend**  
Spring Boot provides means to automatically store and retrieve Java objects into SQL-based databases, i.e., it
automatically translates classes and attributes to tables and rows (object-relational mapping). Here, the entity
class `Task` defines a mapping from attributes to columns, whereas the repository class `TaskRepository` provides
standard database operations for objects of type `Task`, such as, `findById`, `findAll`, `save`, and `delete`. The
services of the application, e.g., the `TaskService`, operate on the entity level and implement domain specific
operations, e.g., method `create` is used to create and persist a new Task object in the database. The controller
classes provide a gateway for the services that can be used by the frontend. The frontend does not operate on the entity
level, but instead uses DTOs (Data Transfer Objects), e.g., `TaskDTO`. Hence, the controllers, e.g., `TaskController`,
map incoming DTO arguments to entities and vice versa. This enforces a clear separation of concerns between the frontend
and backend. The reference to other repositories, services, and controllers set automatically by the framework (
dependency injection).

**javafx frontend**  
JavaFX enables the developer to store the designed UI in an XML file, e.g., `taskCreationView.fxml`. At runtime, the
application automatically constructs the objects required for rendering the UI. Thereby, the view is strictly separated
from the controller logic. However, the controller logic may change the displayed UI, if necessary. In the swtGrocery
application, each tab is described in a XML file and is associated with a view controller,
e.g., `TaskCreationViewController`. The Ui components, such as buttons, labels, etc., defined in the XML file can be
associated with a unique id and methods to be invoked in case of actions. The fields and methods in the respective view
controller are set automatically (dependency injection) to these UI component at runtime. For example, clicking on
button `Create new Task` invokes the method `createNewTask` in the `TaskCreationViewController`.

## Development

1. Import the project located in the `swtGrocery` folder as a Gradle project into IntelliJ. Depending on your IntelliJ
   version, you can do this by just opening the folder, IntelliJ should then automatically configure based on the gradle
   script. This should also work if you open the repository folder in IntelliJ; it should then prompt you that it found
   a gradle build setup.
2. Make sure to start the database (see above) before running the swtGrocery application
3. Make changes to the code and the tests ....
    - Use scene builder to make changes to the FXML files (you might need to refresh your IntelliJ project afterwards)
    - Rerun the `bootRun` gradle task after making changes.
    - When running your software, inspect the database (=the current state of your application) using DBeaver. When
      changing the database schema, you may need to delete the existing database with DBeaver.
    - **Run `npm run autoformat` to automatically format your code**
    - Use the `Analyze > Code Cleanup` and Analyze > Inspect Code` feature from IntelliJ.
4. Test your code by running the unit and integration test cases as follows
    - Naming test cases follows the pattern `<method>Should<expected>(If/For<condition>)`
5. Autoformat your code
    - Setup: Install the node.js dependency, npm will be included. Then run "npm install" in the swtGrocerys folder that
      contains the gradle script and the source code. This may result in package-lock.json changing. This is normal and
      should be committed, but best in a separate commit.
    - To run autoformat, run `npm run autoformat` in the same folder as described for the setup above.

## Build pipeline

The swtGrocery project automatically runs two "jobs" whenever a new change is pushed to the repository:

- `Checkformat`: checks the format of your code (use the `autoformat.sh`/`autoformat.bat` script before pushing your
  changes)
- `Compile`: checks if the project compiles

The result of these can be checked in the gitlab web interface
at https://gitlab.rz.uni-bamberg.de/swt/teaching/2023-ws/swt-swl-b/team-d/-/pipelines

## Software Implementation

### View controller
**Description**

The view controller(s) are used as a connection between the frontend input(GUI) and the backend functionality (services). 
Each view has their own controller.

Currently implemented views & view controller:
- CatalogueViewController

**Structure**

The view controllers implement the interface 'ViewControllerInterface'
The following UML showcases the implemented functions and attributes.
![View Controller UML Class Diagram](https://gitlab.rz.uni-bamberg.de/swt/teaching/2023-ws/swt-swl-b/team-d/-/blob/main/doc/UMLFilesDocumentation/CatalogueViewControllerUML.png)

**Functionality**

- implements CRUD functionality for item entities
- implements search functionality for listview showcasing item entities

### Model
**Description**

In a MVC architecture the model performs buisness logic operations and holds the data. The springBoot framework + JAP already bring a default class structure and functionality. 
[SpringBoot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/)
[Jakarta EE Documentation](https://jakarta.ee/specifications/)

**Structure**

The [UML class diagram](https://gitlab.rz.uni-bamberg.de/swt/teaching/2023-ws/swt-swl-b/team-d/-/blob/main/doc/UMLFilesDocumentation/UMLBackend_Item.png) of the backend showcases the entity 'Item' and its connection to services, repositories, and the accessible interface 'itemcontroller'.

**Functionality**

- The backend implements all functions necessary to CRUD the Item entity. 
- The repository was extended to handle find by name requests (returns item) and search by name requests (returns list).
- The itemcontroller interface exposes CRUD and search functions. The view controllers touch on the itemcontroller and handle itemDTO  (datatransfer objects). 
- The Modelmapper allows to mirror items to itemDTOs, hence, making them available for usage by the view controller.  This decoupling makes allows for secure handling of the items and allows for springboot to handle object dependencies efficiently.

### entities
#### Item

*Update for #63*
Implemented in Merge Request for branch #63.
Requirement:
- Based on change customer change request [Change Request](https://vc.uni-bamberg.de/pluginfile.php/2385522/mod_resource/content/2/SWL-2023-ChangeRequest.pdf)

Changes Overview:
The item entity is extended with a category attribute.
Each item only has one category.
Item creation requires a category.
Changes have been reflected in the database model.

Function Changes:
The itemService is extended by updateItemCategory :: public void updateItemCategory(ItemDTO itemDTO, String itemCategory) throws GenericServiceException;
  - updates the itemCategory
  - no test was written as this function is a void function which relies solely on repository

The item Controller is extended by findByCategory :: public List<ItemDTO> findByCategory(String searchedCategory) throws GenericServiceException;
  - returns a list of all items in this category
  - tested


The AssociationItemUnitService is updated such that create takes item category as parameter ::     public void create(String itemName, List<String> unitNames, String itemCategory) throws GenericServiceException;

The AssociationItemUnitService is extended with findByCategory :: public List<AssociationItemUnit> findByCategory(String itemCategory) throws GenericServiceException;
  - returns a list collection of AssociationItemUnitService entities that carry the category.
  - makes assocationitemunit entities searchable from the frontend

Item category can now be modified through the association entity with:
:: public void updateItemCategory(AssociationItemUnit associationItemUnit, String itemCategory) throws GenericServiceException;
:: public String getItemCategory(AssociationItemUnit associationItemUnit) throws GenericServiceException;

ViewController Changes:
- combobox is updated with the table selection through :: populateComboBoxWithSelectedItemCategory() throws GenericServiceException;
- table headers updated to drop 'item' prefix
- added table column containing an items category
- modifyItemCategory() is added to update the items category in front and backend
- 

FrontEnd Changes:
- added a combobox for dropdown selection in catalogue view.
  - currently the combobox still carries hard coded values set in the catalogue view controller

Other Changes:
- minor bugfixes:
  - removed curly brackets from unit depiction
  - updated itemservice create method to capitalize all new entries
  - updated catalogue view fxml --> fitting user interaction elements into accordion


### Past Purchase List
*Update for #76*

Implemented in Merge Request for branch #76

*Requirements:*

Categories that have been imported through the past purchase import interface shall be selectable in the catalgoue management field.

Categories in the catalogue management interface shall be unique. 

Categories shall be updated during initialization and during import of new past purchase lists.

Default categories shall be extended only by the existing categories of the current past purchase list. If the past purchase list is empty only the default set of categories shall be available to the user. 

*Changes Overview:*
- updated past purchase backend to carry categories
- update past purchase frontend to show categories
- updated catalogueviewcontroller to update categories during initialize()
- updated pastpurchaseviewcontroller to update categories during import process

*Function Changes:*
CatalogueViewController:
- Added public void populateCategoryDropDownMenuWithMissingCategories() to the catalogue view controller
- autowired pastpurchaseitemservice to catalogueviewcontroller to pull all categories in the past purchase list

pastpurchaseviewcontroller:
- calling populateCategoryDropDownMenuWithMissingCategories during import process

Past Purchase List Backend:
- added the item category to all past purchase entity relevant backend structures
- updated pastpurchaseitemservice create() method to take String category
  - updated associated functions such as createWithDate() to work with String category
- updated line validation to validate category as well 
  - affected importPastPurchaseList()
  - affected validateLine()

*Frontend*
- updated the tablecolumn initialization to take into account the category of an item

### Change Request

Issue #86 
#### Requirement
There shall be no catalogue to list menu on the bottom of the catalogue view.
Item shall be added to the grocery list via the grcoery catalogue.

#### Implementation
- removed all catalogue to list related fxml field 
- removed all code bindings related to these fields
- changed some bindings to class attributes (quantity) to ensure the passing of values during method calls

### Change Request 
#85 
#### Requirement
Dropdown menus shall be set according to textfield value. 

#### Implementation
- created bindings between dropdown menu (units) and category combobox with the textfield addingField
- listener constantly checks the adding field entry against the DB, if there is a match the dropdown and the combobox are updated

## CatalogueViewController
### ChangeRequest/#81-Enable-Direct-Item-Addition-To-Grocery-List

### Requirement
*Track*
From customer and client in sprint reivew meeting 4.

*Requirement*
The user shall be able to directly add an item from the grocery catalogue to the grocery list.

*Solution*
User is prompted with choices of units when double clicking an item in grocery catalogue.
Differentiating double click from single click in grocery catalogue table view.
When unit is choosen item will be moved to the grocery list.

### Implementation details
ViewControllerInterface
- created choice dialogue in ViewControllerInterface taking list of arguments prompting user to chose a unit to be used when item is added to the list
CatalogueViewController
- added double click handling to CatalogueViewController
