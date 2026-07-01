# URL Shortener Service

A REST API built with **Java 17** and **Spring Boot** that creates short URLs, redirects to original URLs, tracks click analytics, and supports custom codes with configurable expiry.

## Features

- 🔗 **Shorten URLs** — Generate random 7-character short codes
- 🎯 **Custom Codes** — Choose your own short code (e.g., `amzn`)
- ⏰ **Expiry Support** — Set TTL in minutes for temporary links
- 📊 **Click Analytics** — Track click count per URL
- 🔄 **302 Redirect** — Standard HTTP redirect to original URL

## Tech Stack

- Java 17
- Spring Boot 3.2.5
- Maven
- In-memory storage (ConcurrentHashMap)

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/shorten` | Create a short URL |
| GET | `/{code}` | Redirect to original URL |
| GET | `/stats/{code}` | Get URL click statistics |

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.8+

### Run Locally

```bash
git clone https://github.com/mounikajinkala84-cell/url-shortener.git
cd url-shortener
mvn clean package -DskipTests
java -jar target/url-shortener-1.0-SNAPSHOT.jar
```

Server starts at `http://localhost:8080`

## Usage Examples

### Create a short URL
```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.google.com"}'
```

**Response:**
```json
{
  "shortCode": "sIhlSfX",
  "shortUrl": "http://localhost:8080/sIhlSfX",
  "longUrl": "https://www.google.com",
  "expiresAt": "never"
}
```

### Create with custom code and expiry
```bash
curl -X POST http://localhost:8080/shorten \
  -H "Content-Type: application/json" \
  -d '{"url":"https://www.amazon.com","customCode":"amzn","expiryMinutes":60}'
```

### Redirect
```bash
curl -v http://localhost:8080/amzn
# Returns 302 redirect to https://www.amazon.com
```

### Get statistics
```bash
curl http://localhost:8080/stats/amzn
```

**Response:**
```json
{
  "shortCode": "amzn",
  "longUrl": "https://www.amazon.com",
  "clickCount": 5,
  "createdAt": "2026-07-01T06:30:00Z",
  "expiresAt": "2026-07-01T07:30:00Z"
}
```

## Project Structure

```
src/main/java/com/mounika/urlshortener/
├── UrlShortenerApplication.java       # Spring Boot entry point
├── controller/
│   └── UrlShortenerController.java    # REST endpoints
├── service/
│   └── UrlShortenerService.java       # Business logic
├── repository/
│   └── UrlRepository.java            # In-memory data store
└── model/
    ├── ShortenRequest.java            # Request DTO
    ├── ShortenResponse.java           # Response DTO
    ├── UrlMapping.java                # URL entity
    └── UrlStats.java                  # Stats response DTO
```

## Future Enhancements

- [ ] DynamoDB persistence (replace in-memory store)
- [ ] Deploy to AWS Lambda + API Gateway
- [ ] Rate limiting
- [ ] URL validation
- [ ] Dashboard UI

## Author

**Mounika Jinkala** — [GitHub](https://github.com/mounikajinkala84-cell)
