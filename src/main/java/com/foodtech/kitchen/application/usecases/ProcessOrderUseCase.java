package com.foodtech.kitchen.application.usecases;

import com.foodtech.kitchen.application.ports.in.ProcessOrderPort;
import com.foodtech.kitchen.application.ports.out.OrderRepository;
import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.application.ports.out.TaskRepository;
import com.foodtech.kitchen.domain.model.Order;
import com.foodtech.kitchen.domain.model.Product;
import com.foodtech.kitchen.domain.model.Table;
import com.foodtech.kitchen.domain.model.Task;
import com.foodtech.kitchen.domain.services.TaskDecomposer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Use case for processing customer orders and managing table occupancy.
 * 
 * <p><strong>Architecture:</strong></p>
 * <ul>
 *   <li>Application Layer - Orchestrates domain logic</li>
 *   <li>Implements ProcessOrderPort (input port)</li>
 *   <li>Uses OrderRepository, TaskRepository, TableRepository (output ports)</li>
 * </ul>
 * 
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Save order to database</li>
 *   <li>Decompose order into kitchen tasks</li>
 *   <li>Persist tasks to database</li>
 *   <li>Update table status to OCCUPIED (if table exists)</li>
 *   <li>Link order to table</li>
 * </ul>
 * 
 * <p><strong>Business Rules (HU-007 Integration):</strong></p>
 * <ul>
 *   <li>When an order is created, the table automatically changes to OCCUPIED</li>
 *   <li>The order ID is assigned to the table's currentOrderId</li>
 *   <li>If the table doesn't exist, order is still processed (backward compatibility)</li>
 *   <li>If table is already OCCUPIED, an exception is thrown</li>
 * </ul>
 * 
 * <p><strong>Flow:</strong></p>
 * <ol>
 *   <li>Save order (get order ID from database)</li>
 *   <li>Decompose order into tasks by station</li>
 *   <li>Save all tasks to database</li>
 *   <li>Find table by table number</li>
 *   <li>If table exists and is AVAILABLE, assign order to table (status → OCCUPIED)</li>
 *   <li>Return tasks to caller</li>
 * </ol>
 * 
 * @see ProcessOrderPort
 * @see OrderRepository
 * @see TaskRepository
 * @see TableRepository
 * @see TaskDecomposer
 */
@Service
public class ProcessOrderUseCase implements ProcessOrderPort {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final TaskDecomposer taskDecomposer;
    private final TaskRepository taskRepository;
    private final TableRepository tableRepository;

    /**
     * Constructs the use case with required dependencies.
     *
     * @param orderRepository repository for order persistence
     * @param productRepository repository for product catalog lookup
     * @param taskDecomposer domain service to decompose orders into tasks
     * @param taskRepository repository for task persistence
     * @param tableRepository repository for table management (HU-007)
     */
    public ProcessOrderUseCase(
            OrderRepository orderRepository,
            ProductRepository productRepository,
            TaskDecomposer taskDecomposer,
            TaskRepository taskRepository,
            TableRepository tableRepository
    ) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.taskDecomposer = taskDecomposer;
        this.taskRepository = taskRepository;
        this.tableRepository = tableRepository;
    }

    /**
     * Processes an order by decomposing it into tasks and updating table status.
     * 
     * <p><strong>Steps:</strong></p>
     * <ol>
     *   <li>Persist order to database (generates order ID)</li>
     *   <li>Decompose order into tasks grouped by station</li>
     *   <li>Persist all tasks to database</li>
     *   <li>Update table status to OCCUPIED if table exists</li>
     * </ol>
     * 
     * <p><strong>HU-007 Integration:</strong></p>
     * <p>After creating the order, this use case automatically finds the table
     * by table number and changes its status to OCCUPIED, linking the order ID.</p>
     * 
     * @param order the order to process (must have valid table number)
     * @return list of tasks created for the order
     * @throws IllegalStateException if the table is already OCCUPIED
     */
    @Override
    public List<Task> execute(Order order) {
        // Step 1: Enrich order products with catalog data (preparation time, price, etc.)
        Order enrichedOrder = enrichOrderWithCatalogData(order);

        // Step 2: Save order to get database-generated ID
        Order savedOrder = orderRepository.save(enrichedOrder);

        // Step 3: Decompose order into kitchen tasks by station
        List<Task> tasks = taskDecomposer.decompose(savedOrder);

        // Step 4: Persist all tasks to database
        taskRepository.saveAll(tasks);

        // Step 5: Update table status to OCCUPIED (HU-007)
        updateTableStatusToOccupied(savedOrder);

        return tasks;
    }

    /**
     * Enriches order products with data from the product catalog.
     *
     * <p>For each product in the order, looks up the catalog version by name.
     * If found, uses the catalog product with full details (preparation time, price, etc.).
     * If not found, keeps the original product (backward compatibility).</p>
     *
     * @param order the order with basic product info
     * @return new Order with enriched products from catalog
     */
    private Order enrichOrderWithCatalogData(Order order) {
        List<Product> enrichedProducts = order.getProducts().stream()
                .map(this::findCatalogProductOrKeepOriginal)
                .collect(Collectors.toList());

        return new Order(order.getTableNumber(), enrichedProducts);
    }

    /**
     * Finds a product in the catalog by name, or returns the original if not found.
     *
     * @param product the product to look up
     * @return catalog product if found, original product otherwise
     */
    private Product findCatalogProductOrKeepOriginal(Product product) {
        return productRepository.findByName(product.getName())
                .orElse(product);
    }

    /**
     * Updates the table status to OCCUPIED and assigns the order to it.
     * 
     * <p>This method implements the automatic table status update when an order
     * is created (HU-007 business requirement).</p>
     * 
     * <p><strong>Behavior:</strong></p>
     * <ul>
     *   <li>If table exists and is AVAILABLE → assigns order and changes to OCCUPIED</li>
     *   <li>If table doesn't exist → no action (backward compatibility)</li>
     *   <li>If table exists but is not AVAILABLE → throws IllegalStateException</li>
     * </ul>
     * 
     * @param order the saved order (must have ID)
     * @throws IllegalStateException if table exists but is already OCCUPIED
     */
    private void updateTableStatusToOccupied(Order order) {
        Optional<Table> tableOptional = tableRepository.findByTableNumber(order.getTableNumber());
        
        if (tableOptional.isPresent()) {
            Table table = tableOptional.get();
            
            // The assignOrder method in Table entity will:
            // 1. Validate table is AVAILABLE
            // 2. Set currentOrderId to order ID
            // 3. Change status to OCCUPIED
            // 4. Update updatedAt timestamp
            table.assignOrder(order.getId());
            
            // Persist the updated table
            tableRepository.update(table);
        }
        // If table doesn't exist, order is still processed (no table management)
    }
}