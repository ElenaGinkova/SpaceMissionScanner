# Space Missions Scanner

## About the Project
Space Missions Scanner is a Java project focused on analyzing real-world datasets of space missions and rockets from 1957 onward. It processes information from CSV files and offers multiple statistical and analytical operations over mission and rocket data.

The application can filter missions by status, group them by country, identify the most successful companies within a selected time frame, determine the most commonly used launch locations for each company, rank missions by cost, and evaluate rockets based on height, metadata, and reliability. Rocket reliability is calculated from mission outcomes, allowing the system to determine the most reliable rocket in a given period.

Since real-world datasets often contain missing or incomplete values, the project handles such cases using `Optional`. It also uses custom exceptions for input validation and AES (Rijndael) encryption to securely save confidential results. The project highlights object-oriented design, data parsing, stream-based data processing, exception handling, cryptography, and automated testing.

## Features
- CSV parsing for mission and rocket datasets
- Statistical analysis of missions and rockets
- Mission filtering, grouping, and ranking
- Rocket reliability calculation
- Missing-data handling with `Optional`
- Input validation with custom exceptions
- Secure result storage with AES encryption
- Unit testing for functionality and correctness

## Technologies Used
- Java
- JUnit
- Java Streams
- Records
- Enums
- Optional
- AES / Rijndael
- Object-Oriented Programming
