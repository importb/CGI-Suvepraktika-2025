# CGI Summer Internship Task

This project is a web application developed as a task for the CGI summer internship application process. It allows users to search for flights based on various criteria and then select seats on a visual seat map, receiving recommendations based on their preferences. The application can be run using Docker Compose for easy setup.

## Features

### Flight Selection
*   **View Flights:** Displays an initial list of available flights.
*   **Filtering:** Users can filter the flight list based on:
    *   Destination (e.g., "WAW")
    *   Departure Date
    *   Maximum Flight Duration (in minutes)
    *   Maximum Price (€)
*   **Reset Filters:** A button to clear all applied filters.
*   **Select Flight:** Clicking on a flight row navigates the user to the seat selection view for that specific flight.

### Seat Selection & Recommendation
*   **Visual Seat Map:** Displays a grid representing the aircraft's seating layout for the selected flight.
*   **Passenger Count:** User can specify the number of passengers requiring seats.
*   **Preferences:** Users can indicate preferences for:
    *   Window Seat
    *   Extra Legroom
    *   Seat Near Exit
*   **Seat Recommendations:** Based on selected preferences and passenger count, available seats matching the criteria are highlighted on the map. *(Note: Current recommendation logic might prioritize individual preferences and finding consecutive blocks)*.
*   **Seat Status:** Clearly distinguishes between:
    *   Available Seats
    *   Occupied Seats (Randomly generated/pre-defined for the task)
    *   Recommended Seats
    *   Selected Seats
    *   First Class Seats
*   **Seat Information:** Hovering or clicking provides details (Seat Number, Type, Status). Indicators show Window (W), Legroom (L), Exit (E).
*   **Interactive Selection:** Users can click on available seats to select/deselect them, up to the specified number of passengers.
*   **Dynamic Pricing:** The total price updates based on the number of passengers and whether selected seats are First Class (adds a fixed surcharge per first-class seat).
*   **Booking Confirmation (Simulated):** A confirmation button sends the selected flight and seat data to the backend (currently logs data and shows an alert). Navigation back to the flight search view occurs after confirmation.

### Bonus Features Implemented
*   **Seat Classes:** Includes a basic concept of First Class (Rows 1-2) with an associated price surcharge.
*   **Docker Support:** The application is containerized using Docker and can be easily run with Docker Compose.

## Technologies Used
*   **Backend:**
    *   Java 21
    *   Spring Boot 
    *   Gradle 
*   **Frontend:**
    *   Vue.js
    *   Vite (Build Tool)
    *   Axios
*   **Containerization:**
    *   Docker
*   **Version Control:** Git

## Setup and Running Instructions
### Prerequisites
*   Git
*   Docker

*(Manual setup prerequisites below are only needed if NOT using Docker)*
*   Java JDK 21
*   Gradle
*   Node.js (v18+) and npm

### Running with Docker Compose (Recommended)
1.  **Clone the repository:**
    ```bash
    git clone https://github.com/importb/CGI-Suvepraktika-2025/
    cd CGI-Suvepraktika-2025
    ```
2.  **Ensure Docker Desktop is running.**
3.  **Build and run the services using Docker Compose:**
    ```bash
    docker compose up --build
    ```
4.  **Access the application:**
    *   The frontend should be accessible at `http://localhost:3000`
    *   The backend API will be running internally, but can also be accessed at `http://localhost:8080`
  
### Running Manually (Without Docker)
#### Backend (`flight-planner-backend`)
1.  **Navigate to the backend directory:**
    ```bash
    cd flight-planner-backend
    ```
2.  **Build the project:**
    *   `./gradlew build`
3.  **Run the application:**
    *   `./gradlew bootRun`
4.  The backend should run on `http://localhost:8080`.

#### Frontend (`flight-planner-frontend`)
1.  **Navigate to the frontend directory:**
    ```bash
    cd ../flight-planner-frontend
    ```
2.  **Install dependencies:**
    *   Using npm: `npm install`
3.  **Run the development server:**
    *   Using npm: `npm run dev`
4.  Access the frontend via the URL provided.

## Documentation
### Time Spent
*  Approximately **8** were spent on developing this, most of the time went into making the front-end work.

### Challenges.
* **Front-end:** I rarely develop front-end code, and getting everything to work the way I wanted was a bit of a hassle, but I managed to overcome this by using a lot of Google and some AI tools' help. I didn't copy code 1-to-1, but rather tried to understand what I found and apply it to my use cases.
* **Time:** Because of school, I couldn't leave myself a lot time to develop the flight-planner, there are still a lot of improvement I want to add, but currently has to stay in this state :).

### Future Improvements
* **Transactional Booking**: Currently the backend only calculates the pricing, but doesn't perform a true transactional booking - (checking for availability and persisting the booking for example).
* **Better Seat Recommendation**: The current implementation is really basic, just recommending a available row basically. I'd change it to better handle finding groups of seats together, maybe consider aisle preferencecs, etc.
* **Real Flight Data**: This was one of the features I really wanted to add, but looking into it, I would've had to rewrite a lot of the code.
* **Testing**: Unit and integration tests for both backend and frontend.
* **Authentication/Users(?)**: A way to connect the booking to the user. My idea would be to add Google auth for easy managing or maybe just a simple email verification.

---

*Developed by Rainer Vana*
