MyEcommerce

MyEcommerce is a Spring Boot-based e-commerce platform that allows users to register, browse products, place orders, and process payments securely. It supports multiple authentication methods (Basic Auth, Google OAuth, GitHub OAuth), user profile management, and ADMIN-restricted product management. The app integrates with Stripe for payment processing and tracks order statuses from PENDING to PAID, SHIPPED, and DELIVERED.

# ~~**Features**~~

**_Authentication:_**
Basic Auth: Register or log in with email and password (POST /myEcommerce/createUser, /login).

Google OAuth: Authenticate via Google, creating or linking a user account.

GitHub OAuth: Authenticate via GitHub, creating or linking a user account.

**User Profiles**: Store additional user details (bio, date of birth, phone number) via the UserProfile entity.



**Product Management**: ADMINS can add or delete products (POST /myEcommerce/products, DELETE /myEcommerce/products/{id}).



**Order Management**: Users can browse products, create orders with a shipping address (POST /myEcommerce/orders), and track orders (GET /myEcommerce/orders/{orderId}).



**Payment Processing**: Process payments via Stripe (POST /myEcommerce/payments/process/{orderId}), updating order status to PAID and SHIPPED.



**Delivery Tracking**: Mark orders as DELIVERED (POST /myEcommerce/payments/mark-delivered/{orderId}).



**Error Handling**: Validates orders (stock, address) and handles payment failures with retry options.


**_~~Workflow**_~~

The following flowchart illustrates the user journey in MyEcommerce, from authentication to order delivery:



### Workflow Steps





Start: User accesses the app.



Authentication: Choose Basic Auth, Google OAuth, or GitHub OAuth to register/log in.



Add Shipping Address: Provide a shipping address (POST /myEcommerce/{userId}/address).



Admin Check: If the user is an ADMIN, they can add products (POST /myEcommerce/products).



Browse Products: View available products.



Create Order: Select products, quantities, and address (POST /myEcommerce/orders).



Validate Order: Check stock and address validity.



Process Payment: Use Stripe to process payment (POST /myEcommerce/payments/process/{orderId}).



Payment Outcome: If successful, set status to PAID and SHIPPED; if failed, retry or cancel.



Mark Delivered: Update status to DELIVERED (POST /myEcommerce/payments/mark-delivered/{orderId}).



End: Workflow complete.


Technologies





Spring Boot: Backend framework.



MySQL: Database for users, profiles, products, and orders.



Spring Security: Handles Basic Auth and OAuth2 (Google, GitHub).



Stripe: Payment processing.



Hibernate/JPA: ORM for database operations.



Maven: Dependency management.