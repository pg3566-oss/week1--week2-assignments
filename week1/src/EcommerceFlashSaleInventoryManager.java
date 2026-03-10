import java.util.*;

public class EcommerceFlashSaleInventoryManager{

    HashMap<String, Integer> stockMap = new HashMap<>();
    LinkedHashMap<Integer, String> waitingList = new LinkedHashMap<>();

    // Add product with stock
    public void addProduct(String productId, int stock) {
        stockMap.put(productId, stock);
    }

    // Check stock availability
    public int checkStock(String productId) {
        return stockMap.getOrDefault(productId, 0);
    }

    // Purchase item (thread-safe)
    public synchronized String purchaseItem(String productId, int userId) {

        int stock = stockMap.getOrDefault(productId, 0);

        if(stock > 0) {
            stockMap.put(productId, stock - 1);
            return "Success, " + (stock - 1) + " units remaining";
        }
        else {
            waitingList.put(userId, productId);
            return "Added to waiting list, position #" + waitingList.size();
        }
    }

    public static void main(String[] args) {

        EcommerceFlashSaleInventoryManager system = new EcommerceFlashSaleInventoryManager();

        system.addProduct("IPHONE15_256GB", 100);

        System.out.println("Stock: " + system.checkStock("IPHONE15_256GB"));

        System.out.println(system.purchaseItem("IPHONE15_256GB", 12345));
        System.out.println(system.purchaseItem("IPHONE15_256GB", 67890));
        System.out.println(system.purchaseItem("IPHONE15_256GB", 99999));
    }
}


