# Client Management System

## Overview

This is a comprehensive JavaFX-based Client Management System designed to streamline business operations, including customer management, service requests, technician scheduling, quotes, and payments.

## Features

- 🔐 Secure User Authentication
- 👥 Customer Management
- 🛠️ Service Request Tracking
- 👷 Technician Management
- 💰 Quote and Payment Processing
- 📊 Reporting Capabilities

## Technology Stack

- **Language:** Java 11+
- **UI Framework:** JavaFX
- **Database:** SQLite
- **Build Tool:** Maven
- **Dependencies:**
    - JavaFX Controls
    - SQLite JDBC
    - JavaMail
    - iText PDF Generation

## Prerequisites

- Java Development Kit (JDK) 11 or higher
- Maven
- Git

## Installation

1. Clone the repository:
   ```bash
   git clone git@github.com:Raccoon254/java-client-management.git
   cd java-client-management
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the application:
   ```bash
   mvn javafx:run
   ```

## Default Login Credentials

- **Username:** admin
- **Password:** admin123

⚠️ **Note:** Change the default password after first login!

## Project Structure

```
src/
├── main/
│   ├── java/com/management/
│   │   ├── App.java                   # Main application entry point
│   │   ├── model/                     # Data models
│   │   ├── controller/                # UI controllers
│   │   ├── service/                   # Business logic services
│   │   ├── dao/                       # Data Access Objects
│   │   └── util/                      # Utility classes
│   └── resources/
│       ├── fxml/                      # UI layout files
│       └── images/                    # Application icons
```

## Key Components

- **Models:** Represent data entities (Customer, ServiceRequest, Technician, etc.)
- **Controllers:** Manage UI interactions and data flow
- **Services:** Handle business logic
- **DAO:** Provide database interaction methods
- **Utilities:** Offer helper functions for various tasks

## Database

The application uses SQLite with an embedded database (`client_management.db`). Tables include:
- Users
- Customers
- Technicians
- Service Requests
- Quotes
- Payments

## Contributing

We welcome contributions! Here's how you can help:

1. Fork the repository
2. Create a new branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

### Contribution Guidelines

- Follow existing code style
- Write clear, concise commit messages
- Include unit tests for new features
- Update documentation as needed

## Planned Improvements

- [ ] Add unit and integration tests
- [ ] Implement more robust error handling
- [ ] Create comprehensive logging
- [ ] Develop advanced reporting features
- [ ] Enhance UI/UX

## License

Distributed under the MIT License. See `LICENSE` for more information.

## Contact

Project Maintainer: [Steve Tom/tomsteve187@gmail.com]

## Acknowledgments

- JavaFX Team
- SQLite
- Maven Community
- All contributors and supporters

---

**Star ⭐ the project if you find it helpful!**