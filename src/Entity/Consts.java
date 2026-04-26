package Entity;

import java.net.URLDecoder;
import java.io.File;

public class Consts {
    
    private Consts() {
        throw new AssertionError();
    }
    
    protected static final String DB_FILEPATH = getDBPath();
    public static final String CONN_STR = "jdbc:ucanaccess://" + DB_FILEPATH + ";COLUMNORDER=DISPLAY";
    
    /*----------------------------------------- INVENTORY QUERIES -----------------------------------------*/
    public static final String SQL_SEL_INVENTORY = "SELECT * FROM [Inventory Item]";
    public static final String SQL_DEL_INVENTORY = "{ call Remove_Inventory(?) }";
    public static final String SQL_ADD_INVENTORY = "{ call Add_Inventory(?, ?, ?, ?, ?, ?, ?, ?) }";
    public static final String SQL_UPD_INVENTORY = "{ call Update_Inventory(?, ?, ?, ?, ?, ?, ?, ?) }";
    public static final String SQL_FIND_BY_ID_INVENTORY = "SELECT * FROM [Inventory Item] WHERE InventoryItemID = ?";
    /*----------------------------------------- SUPPLIER QUERIES --------------------------------------------*/
    public static final String SQL_SEL_SUPPLIER = "SELECT * FROM Supplier";
    public static final String SQL_UPD_SUPPLIER = "{ call Update_Supplier(?, ?, ?) }";
    public static final String SQL_ADD_SUPPLIER = "{ call Add_Supplier(?, ?, ?) }";
    public static final String SQL_DEL_SUPPLIER = "{ call Remove_Supplier(?) }";
    public static final String SQL_FIND_BY_ID_SUPPLIER = "{ call Find_Supplier_By_ID(?) }";
    
    /**
     * find the correct path of the DB file
     * @return the path of the DB file (from eclipse or with runnable file)
     */
    private static String getDBPath() {
        try {
            // Method 1: Try working directory first
            String workingDir = System.getProperty("user.dir");
            System.out.println("Consts - Working directory: " + workingDir);
            
            String[] workingDirPaths = {
                workingDir + File.separator + "ex1_database_2025_RT2.accdb",
                workingDir + File.separator + "DentalCare" + File.separator + "ex1_database_2025_RT2.accdb",
                "ex1_database_2025_RT2.accdb"
            };
            
            for (String testPath : workingDirPaths) {
                File testFile = new File(testPath);
                System.out.println("Consts - Testing working dir path: " + testPath + " - exists: " + testFile.exists());
                if (testFile.exists()) {
                    System.out.println("Consts - Found database at: " + testPath);
                    return testPath;
                }
            }
            
            // Method 2: Try class location method
            String path = Consts.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decoded = URLDecoder.decode(path, "UTF-8");
            
            System.out.println("Consts - Debug - Original path: " + path);
            System.out.println("Consts - Debug - Decoded path: " + decoded);
            
            if (decoded.contains(".jar")) {
                // Running from JAR file
                decoded = decoded.substring(0, decoded.lastIndexOf('/'));
                String jarPath = decoded + "/ex1_database_2025_RT2.accdb";
                System.out.println("Consts - JAR mode, trying: " + jarPath);
                return jarPath;
            } else {
                // Running from Eclipse/IDE
                String basePath;
                if (decoded.contains("bin/")) {
                    basePath = decoded.substring(0, decoded.lastIndexOf("bin/"));
                } else if (decoded.contains("DentalCare/")) {
                    basePath = decoded.substring(0, decoded.indexOf("DentalCare/"));
                } else {
                    basePath = decoded;
                }
                
                // Try multiple possible locations for the database
                String[] possiblePaths = {
                    basePath + "ex1_database_2025_RT2.accdb",                    // Workspace root
                    basePath + "DentalCare" + File.separator + "ex1_database_2025_RT2.accdb",  // Project folder
                    basePath + "lib" + File.separator + "ex1_database_2025_RT2.accdb",        // In lib folder
                    basePath + "database" + File.separator + "ex1_database_2025_RT2.accdb",   // In database folder
                    basePath + "src" + File.separator + "ex1_database_2025_RT2.accdb"         // In src folder
                };
                
                System.out.println("Consts - IDE mode, base path: " + basePath);
                
                for (String testPath : possiblePaths) {
                    File testFile = new File(testPath);
                    System.out.println("Consts - Testing class path: " + testPath + " - exists: " + testFile.exists());
                    if (testFile.exists()) {
                        System.out.println("Consts - Found database at: " + testPath);
                        return testPath;
                    }
                }
                
                // Default fallback
                String defaultPath = basePath + "ex1_database_2025_RT2.accdb";
                System.out.println("Consts - Using default path: " + defaultPath);
                return defaultPath;
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Final fallback to relative path
            String fallbackPath = "ex1_database_2025_RT2.accdb";
            System.out.println("Consts - Exception occurred, using fallback: " + fallbackPath);
            return fallbackPath;
        }
    }
}