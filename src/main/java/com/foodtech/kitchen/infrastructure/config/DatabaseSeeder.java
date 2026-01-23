package com.foodtech.kitchen.infrastructure.config;

import com.foodtech.kitchen.application.ports.out.ProductRepository;
import com.foodtech.kitchen.application.ports.out.TableRepository;
import com.foodtech.kitchen.domain.model.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Database seeder for development and local environments.
 *
 * <p>This component automatically populates the database with sample data
 * when the application starts in 'local' or 'dev' profile.</p>
 *
 * <p><strong>Data Created:</strong></p>
 * <ul>
 *   <li>Sample products in the catalog (drinks, pastries, sandwiches)</li>
 *   <li>Sample tables (1-10)</li>
 * </ul>
 *
 * <p><strong>Note:</strong> This seeder only runs when the 'local' profile is active.
 * Add -Dspring.profiles.active=local to enable it.</p>
 */
@Component
@Profile("local")
public class DatabaseSeeder implements CommandLineRunner {

    private final ProductRepository productRepository;
    private final TableRepository tableRepository;

    public DatabaseSeeder(ProductRepository productRepository, TableRepository tableRepository) {
        this.productRepository = productRepository;
        this.tableRepository = tableRepository;
    }

    @Override
    public void run(String... args) {
        System.out.println("\n🌱 Starting database seeding...\n");

        seedProducts();
        seedTables();

        System.out.println("\n✅ Database seeding completed!\n");
    }

    private void seedProducts() {
        System.out.println("📦 Seeding products...");

        List<Product> products = Arrays.asList(
            // DRINKS
            createProduct(
                "Espresso",
                "Strong Italian coffee",
                ProductType.DRINK,
                new BigDecimal("2.50"),
                30,
                "https://images.unsplash.com/photo-1510591509098-f4fdc6d0ff04?w=400"
            ),
            createProduct(
                "Cappuccino",
                "Espresso with steamed milk and foam",
                ProductType.DRINK,
                new BigDecimal("3.50"),
                45,
                "https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=400"
            ),
            createProduct(
                "Latte",
                "Espresso with steamed milk",
                ProductType.DRINK,
                new BigDecimal("3.75"),
                45,
                "https://images.unsplash.com/photo-1561882468-9110e03e0f78?w=400"
            ),
            createProduct(
                "Orange Juice",
                "Freshly squeezed orange juice",
                ProductType.DRINK,
                new BigDecimal("4.00"),
                60,
                "https://images.unsplash.com/photo-1600271886742-f049cd451bba?w=400"
            ),
            createProduct(
                "Iced Tea",
                "Refreshing iced tea with lemon",
                ProductType.DRINK,
                new BigDecimal("2.75"),
                30,
                "https://images.unsplash.com/photo-1556679343-c7306c1976bc?w=400"
            ),

            // PASTRIES
            createProduct(
                "Croissant",
                "Buttery French pastry",
                ProductType.PASTRY,
                new BigDecimal("3.00"),
                180,
                "https://images.unsplash.com/photo-1555507036-ab1f4038808a?w=400"
            ),
            createProduct(
                "Chocolate Muffin",
                "Moist chocolate chip muffin",
                ProductType.PASTRY,
                new BigDecimal("3.50"),
                240,
                "https://images.unsplash.com/photo-1607958996333-41aef7caefaa?w=400"
            ),
            createProduct(
                "Blueberry Scone",
                "Fresh baked scone with blueberries",
                ProductType.PASTRY,
                new BigDecimal("3.25"),
                210,
                "https://images.unsplash.com/photo-1586985289688-ca3cf47d3e6e?w=400"
            ),
            createProduct(
                "Cinnamon Roll",
                "Sweet roll with cinnamon and icing",
                ProductType.PASTRY,
                new BigDecimal("4.00"),
                300,
                "https://images.unsplash.com/photo-1626787549638-efa6abae5f10?w=400"
            ),
            createProduct(
                "Danish Pastry",
                "Flaky pastry with fruit filling",
                ProductType.PASTRY,
                new BigDecimal("3.75"),
                270,
                "https://images.unsplash.com/photo-1558618666-fcd25c85cd64?w=400"
            ),

            // SANDWICHES
            createProduct(
                "Club Sandwich",
                "Triple-decker with turkey, bacon, lettuce and tomato",
                ProductType.SANDWICH,
                new BigDecimal("8.50"),
                420,
                "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400"
            ),
            createProduct(
                "BLT",
                "Classic bacon, lettuce and tomato",
                ProductType.SANDWICH,
                new BigDecimal("7.00"),
                300,
                "https://images.unsplash.com/photo-1553909489-cd47e0907980?w=400"
            ),
            createProduct(
                "Grilled Cheese",
                "Melted cheese on toasted bread",
                ProductType.SANDWICH,
                new BigDecimal("6.00"),
                240,
                "https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400"
            ),
            createProduct(
                "Veggie Wrap",
                "Fresh vegetables in a tortilla wrap",
                ProductType.SANDWICH,
                new BigDecimal("7.50"),
                270,
                "https://images.unsplash.com/photo-1626700051175-6818013e1d4f?w=400"
            ),
            createProduct(
                "Caesar Salad",
                "Romaine lettuce with Caesar dressing and croutons",
                ProductType.SANDWICH,
                new BigDecimal("8.00"),
                180,
                "https://images.unsplash.com/photo-1546793665-c74683f339c1?w=400"
            ),
            createProduct(
                "Chicken Panini",
                "Grilled chicken with pesto and mozzarella",
                ProductType.SANDWICH,
                new BigDecimal("9.00"),
                360,
                "https://images.unsplash.com/photo-1509722747041-616f39b57569?w=400"
            )
        );

        int savedCount = 0;
        for (Product product : products) {
            try {
                // Only save if product doesn't exist
                if (!productRepository.existsByName(product.getName())) {
                    productRepository.save(product);
                    savedCount++;
                    System.out.println("  ✓ Created: " + product.getName() + " (" + product.getType() + ")");
                } else {
                    System.out.println("  ⊘ Skipped: " + product.getName() + " (already exists)");
                }
            } catch (Exception e) {
                System.out.println("  ✗ Failed: " + product.getName() + " - " + e.getMessage());
            }
        }

        System.out.println("📦 Products seeded: " + savedCount + "/" + products.size() + "\n");
    }

    private Product createProduct(String name, String description, ProductType type,
                                 BigDecimal price, int prepTimeSeconds, String imageUrl) {
        Product product = new Product(name, description, type, new Price(price), prepTimeSeconds);
        product.setImageUrl(imageUrl);
        return product;
    }

    private void seedTables() {
        System.out.println("🪑 Seeding tables...");

        int savedCount = 0;
        for (int i = 1; i <= 10; i++) {
            String tableNumber = String.valueOf(i);
            int capacity = (i <= 4) ? 2 : (i <= 8) ? 4 : 6; // Tables 1-4: 2 seats, 5-8: 4 seats, 9-10: 6 seats

            try {
                // Only save if table doesn't exist
                if (tableRepository.findByTableNumber(tableNumber).isEmpty()) {
                    Table table = new Table(tableNumber, capacity);
                    tableRepository.save(table);
                    savedCount++;
                    System.out.println("  ✓ Created table: " + tableNumber + " (capacity: " + capacity + ")");
                } else {
                    System.out.println("  ⊘ Skipped table: " + tableNumber + " (already exists)");
                }
            } catch (Exception e) {
                System.out.println("  ✗ Failed table: " + tableNumber + " - " + e.getMessage());
            }
        }

        System.out.println("🪑 Tables seeded: " + savedCount + "/10\n");
    }
}
