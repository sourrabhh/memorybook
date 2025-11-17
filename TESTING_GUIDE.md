# MemoryBook - Testing Guide

## Prerequisites

1. **PostgreSQL Database**
   - Make sure PostgreSQL is running
   - Database: `MemoryBook`
   - Username: `postgres`
   - Password: `password` (or update in `application.properties`)

2. **Application Running**
   - Start the Spring Boot application
   - Default port: `8080`

---

## Step 1: Start the Application

```bash
# Navigate to project directory
cd "/Users/saurabh/Developer/spring boot/memorybook"

# Run the application
./mvnw spring-boot:run

# Or if using Maven directly
mvn spring-boot:run
```

Wait for the application to start. You should see:
```
Started MemorybookApplication in X.XXX seconds
```

---

## Step 2: Create a User (Signup)

First, create a user account:

```bash
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "password": "password123",
    "role": "USER"
  }'
```

**Expected Response:**
```json
"User registered successfully"
```

---

## Step 3: Login to Get JWT Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john.doe@example.com",
    "password": "password123"
  }'
```

**Expected Response:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTYxNjIzOTAyMiwiZXhwIjoxNjE2MjM5MzIyfQ...
```

**Copy the token!** You'll need it for all authenticated requests.

**Note:** Token expires in 5 minutes. Login again if it expires.

---

## Step 4: Test Content Sharing (Main Feature)

### Share Article 1

```bash
curl -X POST http://localhost:8080/api/content/share \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "ARTICLE",
    "title": "Introduction to Spring Boot",
    "description": "Learn the basics of Spring Boot framework",
    "textContent": "Spring Boot is a powerful framework that simplifies Java development. It provides auto-configuration and convention over configuration approach.",
    "source": "Tech Blog"
  }'
```

**Expected Response:**
```json
{
  "contentId": 1,
  "memory": {
    "id": 1,
    "userId": 1,
    "context": "spring, boot, framework, java, development",
    "content": "Introduction to Spring Boot\nLearn the basics of Spring Boot framework\nSpring Boot is a powerful framework...",
    "summary": "Spring Boot is a powerful framework...",
    "suggestions": "Related content you've shared: 'Introduction to Spring Boot'.",
    "relatedContentIds": "1",
    "relevanceScore": 1,
    "keywords": "spring, boot, framework, java, development, powerful, simplifies",
    "type": "Content"
  },
  "newMemory": true,
  "extractedContext": "spring, boot, framework, java, development",
  "suggestions": "...",
  "summary": "..."
}
```

**Note:** `newMemory: true` means a new memory was created.

---

### Share Article 2 (Similar Context - Should Update Memory)

```bash
curl -X POST http://localhost:8080/api/content/share \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "ARTICLE",
    "title": "Spring Boot Best Practices",
    "description": "Essential tips for Spring Boot development",
    "textContent": "Spring Boot offers many best practices including dependency management, configuration properties, and testing strategies.",
    "source": "Dev Blog"
  }'
```

**Expected Response:**
```json
{
  "contentId": 2,
  "memory": {
    "id": 1,
    "userId": 1,
    "context": "spring, boot, framework, java, development, spring, boot, practices, development",
    "content": "Introduction to Spring Boot\n...\n---\n\nSpring Boot Best Practices\nEssential tips for Spring Boot development\n...",
    "summary": "...",
    "suggestions": "This topic has been referenced 2 times. Related content you've shared: 'Introduction to Spring Boot', 'Spring Boot Best Practices'.",
    "relatedContentIds": "1,2",
    "relevanceScore": 2,
    "keywords": "spring, boot, framework, java, development, practices, essential, tips",
    "type": "Content"
  },
  "newMemory": false,
  "extractedContext": "spring, boot, practices, development",
  "suggestions": "...",
  "summary": "..."
}
```

**Note:** 
- Same memory ID (1) was updated
- `newMemory: false` means existing memory was updated
- `relevanceScore` increased to 2
- `relatedContentIds` now contains "1,2"

---

### Share Article 3 (Different Context - Should Create New Memory)

```bash
curl -X POST http://localhost:8080/api/content/share \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "ARTICLE",
    "title": "Getting Started with React",
    "description": "Learn React fundamentals",
    "textContent": "React is a JavaScript library for building user interfaces. It uses components and virtual DOM.",
    "source": "Frontend Blog"
  }'
```

**Expected Response:**
```json
{
  "contentId": 3,
  "memory": {
    "id": 2,
    "userId": 1,
    "context": "react, javascript, library, building, interfaces, components, virtual, dom",
    "content": "...",
    "relevanceScore": 1,
    "relatedContentIds": "3"
  },
  "newMemory": true,
  ...
}
```

**Note:** New memory ID (2) was created because context is different.

---

## Step 5: Test Content Endpoints

### Get All My Shared Content

```bash
curl -X GET http://localhost:8080/api/content/my-content \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "type": "ARTICLE",
    "title": "Introduction to Spring Boot",
    ...
  },
  {
    "id": 2,
    "userId": 1,
    "type": "ARTICLE",
    "title": "Spring Boot Best Practices",
    ...
  },
  ...
]
```

---

### Get Content by Type

```bash
curl -X GET http://localhost:8080/api/content/my-content/type/ARTICLE \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### Search Content

```bash
curl -X GET "http://localhost:8080/api/content/my-content/search?query=spring" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Step 6: Test Memory Endpoints

### Get All Memories

```bash
curl -X GET http://localhost:8080/api/memories \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Expected Response:**
```json
[
  {
    "id": 1,
    "userId": 1,
    "context": "spring, boot, framework, java, development...",
    "content": "...",
    "summary": "...",
    "suggestions": "This topic has been referenced 2 times...",
    "relatedContentIds": "1,2",
    "relevanceScore": 2,
    "keywords": "spring, boot, framework...",
    ...
  },
  {
    "id": 2,
    "userId": 1,
    "context": "react, javascript, library...",
    ...
  }
]
```

---

### Get Memory by ID

```bash
curl -X GET http://localhost:8080/api/memories/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### Search Memories

```bash
curl -X GET "http://localhost:8080/api/memories/search?query=spring" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### Get Memories by Context

```bash
curl -X GET "http://localhost:8080/api/memories/context/spring" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

### Create Memory Manually

```bash
curl -X POST http://localhost:8080/api/memories \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "context": "Python Programming",
    "type": "Note",
    "content": "Python is a versatile programming language"
  }'
```

---

### Update Memory

```bash
curl -X PUT http://localhost:8080/api/memories/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "context": "Updated Spring Boot Context",
    "content": "Updated content about Spring Boot"
  }'
```

---

### Delete Memory

```bash
curl -X DELETE http://localhost:8080/api/memories/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Step 7: Test Different Content Types

### Share a Blog Post

```bash
curl -X POST http://localhost:8080/api/content/share \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "BLOG",
    "title": "My Journey with Spring Boot",
    "description": "Personal experience learning Spring Boot",
    "textContent": "I started learning Spring Boot last year...",
    "source": "Personal Blog"
  }'
```

---

### Share a News Article

```bash
curl -X POST http://localhost:8080/api/content/share \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "NEWS",
    "title": "New Spring Boot Release",
    "description": "Latest updates in Spring Boot 3.0",
    "textContent": "Spring Boot 3.0 introduces new features...",
    "source": "Tech News"
  }'
```

---

### Share a Link

```bash
curl -X POST http://localhost:8080/api/content/share \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "LINK",
    "title": "Spring Boot Documentation",
    "description": "Official Spring Boot documentation",
    "url": "https://spring.io/projects/spring-boot",
    "source": "Spring Official"
  }'
```

---

## Testing Scenarios

### Scenario 1: Sequential Similar Content
1. Share "Spring Boot Basics" → Creates Memory A
2. Share "Spring Boot Advanced" → Updates Memory A
3. Share "Spring Boot Security" → Updates Memory A
4. Result: Memory A has relevanceScore = 3, contains 3 related content IDs

### Scenario 2: Different Topics
1. Share "React Tutorial" → Creates Memory A
2. Share "Vue.js Guide" → Creates Memory B (different context)
3. Share "React Hooks" → Updates Memory A (similar to first)
4. Result: 2 memories with different contexts

### Scenario 3: Search and Filter
1. Share multiple articles on various topics
2. Search for "spring" → Should return Spring-related memories
3. Filter by context → Should return memories matching context

---

## Using Postman

### Import Collection

1. Open Postman
2. Click **Import**
3. Create a new collection: **MemoryBook API**

### Setup Environment Variables

Create an environment with:
- `base_url`: `http://localhost:8080`
- `token`: (will be set after login)

### Create Requests

1. **Auth - Signup**
   - Method: POST
   - URL: `{{base_url}}/api/auth/signup`
   - Body: JSON (raw)
   
2. **Auth - Login**
   - Method: POST
   - URL: `{{base_url}}/api/auth/login`
   - Body: JSON (raw)
   - Tests: Set `token` variable from response

3. **Content - Share**
   - Method: POST
   - URL: `{{base_url}}/api/content/share`
   - Headers: `Authorization: Bearer {{token}}`
   - Body: JSON (raw)

4. **Memories - Get All**
   - Method: GET
   - URL: `{{base_url}}/api/memories`
   - Headers: `Authorization: Bearer {{token}}`

---

## Common Issues & Solutions

### Issue: "User not authenticated"
**Solution:** 
- Make sure you're including the JWT token in the Authorization header
- Token format: `Bearer YOUR_TOKEN`
- Token might have expired (5 minutes), login again

### Issue: "Memory not found"
**Solution:**
- Check if the memory ID exists
- Make sure you're querying memories for the correct user

### Issue: "Not authorized to update this memory"
**Solution:**
- You can only update/delete memories that belong to your user account
- Check the memory's userId matches your user ID

### Issue: Database connection error
**Solution:**
- Make sure PostgreSQL is running
- Check database name, username, password in `application.properties`
- Create database if it doesn't exist: `CREATE DATABASE MemoryBook;`

### Issue: Port already in use
**Solution:**
- Change port in `application.properties`: `server.port=8081`
- Or stop the process using port 8080

---

## Quick Test Script

Save this as `test.sh`:

```bash
#!/bin/bash

BASE_URL="http://localhost:8080"

echo "1. Signup"
curl -X POST $BASE_URL/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"firstName":"Test","lastName":"User","email":"test@example.com","password":"test123"}'

echo -e "\n\n2. Login"
TOKEN=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test@example.com","password":"test123"}')

echo "Token: $TOKEN"

echo -e "\n\n3. Share Content"
curl -X POST $BASE_URL/api/content/share \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "ARTICLE",
    "title": "Test Article",
    "description": "Test Description",
    "textContent": "Test content for testing purposes"
  }'

echo -e "\n\n4. Get Memories"
curl -X GET $BASE_URL/api/memories \
  -H "Authorization: Bearer $TOKEN"

echo -e "\n"
```

Make it executable and run:
```bash
chmod +x test.sh
./test.sh
```

---

## Database Verification

Connect to PostgreSQL and check tables:

```sql
-- Check users
SELECT * FROM users;

-- Check contents
SELECT id, user_id, type, title, extracted_context FROM contents;

-- Check memories
SELECT id, user_id, context, relevance_score, related_content_ids FROM memories;

-- Check memory updates
SELECT id, context, relevance_score, last_related_content_at, updated_at 
FROM memories 
ORDER BY updated_at DESC;
```

---

## Expected Behavior Summary

1. **First content share** → Creates new memory, `relevanceScore = 1`
2. **Similar content share** → Updates existing memory, `relevanceScore++`, adds to `relatedContentIds`
3. **Different content share** → Creates new memory
4. **Search** → Returns memories/content matching query
5. **Context matching** → Finds memories with ≥30% similarity

---

## Next Steps

- Test with more complex content
- Verify memory updates are working correctly
- Test edge cases (empty content, special characters, etc.)
- Check database relationships and data integrity

Happy Testing! 🚀

