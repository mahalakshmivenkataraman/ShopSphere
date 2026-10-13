# ShopSphere UI and Catalog Enhancements

## Product visuals
- Bundled 108 product-specific PNG illustrations, designed as category-relevant clipart, so the seeded catalog has an image for every product without requiring internet access at runtime.
- Products show images in the catalog cards and product details.
- If an image is missing, the app draws a category-themed fallback illustration.
- Sellers can use **Choose Product Image...** when adding a product. PNG, JPG, JPEG, and GIF are supported; the chosen file is copied into `assets/product-images` and its path is saved with the product.

## Product details and recommendations
- Click a product card, image, name, category, or price, or use **View Details**.
- The details view shows image, name, category, INR price, stock availability, description, and up to three related products from the same category.
- Selecting a suggested item opens that item's details.

## Themes
- Use the **Light Theme** / **Dark Theme** button in the dashboard header to switch themes.

## Checkout
- **Proceed to Checkout** opens an order-review screen with item quantities, payment method, coupon, and total.
- The separate **Pay ₹...** button begins the prototype's simulated payment flow. A final confirmation is shown after processing.

## Currency
- Catalog, cart, checkout review, and order history display prices in Indian Rupees (₹).
- Seed data is stored in INR. Older serialized product records without an INR currency marker are converted once on load.
- No real payment provider is contacted.
