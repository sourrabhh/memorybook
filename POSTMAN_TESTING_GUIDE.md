# MemoryBook - Postman Testing Guide

## Step 1: Import Postman Collection

1. Open Postman
2. Click **Import** (top left)
3. Select **File** tab
4. Import the `MemoryBook.postman_collection.json` file (see below)
5. Or click **Raw** and paste the JSON collection

---

## Step 2: Setup Environment Variables

1. Click **Environments** (left sidebar)
2. Click **+** to create new environment
3. Name it: **MemoryBook Local**
4. Add these variables:
   - `base_url`: `http://localhost:8080`
   - `token`: (leave empty, will be auto-filled after login)
   - `user_id`: (leave empty, for reference)
5. Click **Save**
6. Select **MemoryBook Local** from the environment dropdown (top right)

---

## Step 3: Start Your Application

Make sure your Spring Boot application is running:

```bash
./mvnw spring-boot:run
# or
mvn spring-boot:run
```

Wait until you see: `Started MemorybookApplication`

---

## Step 4: Test Step by Step

### 4.1 Signup (Create User)

1. Open the **Auth** folder in the collection
2. Click **1. Signup**
3. Click **Send**
4. You should see:
   ```json
   "User registered successfully"
   ```

**Request Body:**
```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "password123",
  "role": "USER"
}
```

---

### 4.2 Login (Get JWT Token)

1. Click **2. Login**
2. Click **Send**
3. Copy the token from the response
4. The token should be automatically saved (if you set up Tests script)

**Request Body:**
```json
{
  "username": "john.doe@example.com",
  "password": "password123"
}
```

**Expected Response:**
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTYxNjIzOTAyMiwiZXhwIjoxNjE2MjM5MzIyfQ...
```

**Note:** Token expires in 5 minutes. Login again if needed.

---

### 4.3 Share Content (Main Feature)

1. Open the **Content** folder
2. Click **3. Share Content - Article 1**
3. Make sure the `Authorization` header has: `Bearer {{token}}`
4. Click **Send**

**Request Body:**
```json
{
  "type": "ARTICLE",
  "title": "Introduction to Spring Boot",
  "description": "Learn the basics of Spring Boot framework",
  "textContent": "Spring Boot is a powerful framework that simplifies Java development. It provides auto-configuration and convention over configuration approach.",
  "source": "Tech Blog"
}
```

**Expected Response:**
```json
{
  "contentId": 1,
  "memory": {
    "id": 1,
    "userId": 1,
    "context": "spring, boot, framework, java, development",
    "content": "Introduction to Spring Boot\nLearn the basics...",
    "summary": "...",
    "suggestions": "...",
    "relatedContentIds": "1",
    "relevanceScore": 1,
    "keywords": "spring, boot, framework, java, development",
    "newMemory": true
  },
  "extractedContext": "spring, boot, framework, java, development"
}
```

**Check:** `newMemory: true` means a new memory was created.

---

### 4.4 Share Similar Content (Should Update Memory)

1. Click **4. Share Content - Article 2 (Similar)**
2. Click **Send**

**Request Body:**
```json
{
  "type": "ARTICLE",
  "title": "Spring Boot Best Practices",
  "description": "Essential tips for Spring Boot development",
  "textContent": "Spring Boot offers many best practices including dependency management, configuration properties, and testing strategies.",
  "source": "Dev Blog"
}
```

**Expected Response:**
```json
{
  "contentId": 2,
  "memory": {
    "id": 1,
    "relevanceScore": 2,
    "relatedContentIds": "1,2",
    "newMemory": false
  }
}
```

**Check:** 
- Same memory ID (1) 
- `newMemory: false` means existing memory was updated
- `relevanceScore: 2` (incremented)
- `relatedContentIds: "1,2"` (both content IDs)

---

### 4.5 Share Different Content (Should Create New Memory)

1. Click **5. Share Content - Different Topic**
2. Click **Send**

**Request Body:**
```json
{
  "type": "ARTICLE",
  "title": "Getting Started with React",
  "description": "Learn React fundamentals",
  "textContent": "React is a JavaScript library for building user interfaces. It uses components and virtual DOM.",
  "source": "Frontend Blog"
}
```

**Expected Response:**
```json
{
  "contentId": 3,
  "memory": {
    "id": 2,
    "context": "react, javascript, library, building, interfaces",
    "relevanceScore": 1,
    "newMemory": true
  }
}
```

**Check:** New memory ID (2) was created because context is different.

---

### 4.6 Get All My Content

1. Click **6. Get My Content**
2. Click **Send**

**Expected Response:**
```json
[
  {
    "id": 1,
    "type": "ARTICLE",
    "title": "Introduction to Spring Boot",
    ...
  },
  {
    "id": 2,
    "type": "ARTICLE",
    "title": "Spring Boot Best Practices",
    ...
  },
  ...
]
```

---

### 4.7 Get Content by Type

1. Click **7. Get Content by Type - ARTICLE**
2. Click **Send**

---

### 4.8 Search Content

1. Click **8. Search Content**
2. In the URL, replace `spring` with your search query
3. Click **Send**

---

### 4.9 Get All Memories

1. Open the **Memories** folder
2. Click **9. Get All Memories**
3. Click **Send**

**Expected Response:**
```json
[
  {
    "id": 1,
    "context": "spring, boot, framework...",
    "relevanceScore": 2,
    "relatedContentIds": "1,2",
    ...
  },
  {
    "id": 2,
    "context": "react, javascript...",
    "relevanceScore": 1,
    ...
  }
]
```

---

### 4.10 Get Memory by ID

1. Click **10. Get Memory by ID**
2. Replace `1` with the memory ID you want
3. Click **Send**

---

### 4.11 Search Memories

1. Click **11. Search Memories**
2. Replace `spring` with your search query
3. Click **Send**

---

### 4.12 Get Memories by Context

1. Click **12. Get Memories by Context**
2. Replace `spring` with the context you want
3. Click **Send**

---

### 4.13 Create Memory Manually

1. Click **13. Create Memory**
2. Click **Send**

**Request Body:**
```json
{
  "context": "Python Programming",
  "type": "Note",
  "content": "Python is a versatile programming language"
}
```

---

### 4.14 Update Memory

1. Click **14. Update Memory**
2. Replace `1` with the memory ID you want to update
3. Modify the request body
4. Click **Send**

---

### 4.15 Delete Memory

1. Click **15. Delete Memory**
2. Replace `1` with the memory ID you want to delete
3. Click **Send**

---

## Postman Collection JSON

Save this as `MemoryBook.postman_collection.json` and import it:

```json
{
  "info": {
    "name": "MemoryBook API",
    "description": "Complete API collection for MemoryBook application",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "variable": [
    {
      "key": "base_url",
      "value": "http://localhost:8080"
    },
    {
      "key": "token",
      "value": ""
    }
  ],
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "1. Signup",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"firstName\": \"John\",\n  \"lastName\": \"Doe\",\n  \"email\": \"john.doe@example.com\",\n  \"password\": \"password123\",\n  \"role\": \"USER\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/auth/signup",
              "host": ["{{base_url}}"],
              "path": ["api", "auth", "signup"]
            }
          }
        },
        {
          "name": "2. Login",
          "event": [
            {
              "listen": "test",
              "script": {
                "exec": [
                  "if (pm.response.code === 200) {",
                  "    var token = pm.response.text();",
                  "    pm.environment.set('token', token);",
                  "    console.log('Token saved:', token);",
                  "}"
                ]
              }
            }
          ],
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"username\": \"john.doe@example.com\",\n  \"password\": \"password123\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/auth/login",
              "host": ["{{base_url}}"],
              "path": ["api", "auth", "login"]
            }
          }
        }
      ]
    },
    {
      "name": "Content",
      "item": [
        {
          "name": "3. Share Content - Article 1",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"type\": \"ARTICLE\",\n  \"title\": \"Introduction to Spring Boot\",\n  \"description\": \"Learn the basics of Spring Boot framework\",\n  \"textContent\": \"Spring Boot is a powerful framework that simplifies Java development. It provides auto-configuration and convention over configuration approach.\",\n  \"source\": \"Tech Blog\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/content/share",
              "host": ["{{base_url}}"],
              "path": ["api", "content", "share"]
            }
          }
        },
        {
          "name": "4. Share Content - Article 2 (Similar)",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"type\": \"ARTICLE\",\n  \"title\": \"Spring Boot Best Practices\",\n  \"description\": \"Essential tips for Spring Boot development\",\n  \"textContent\": \"Spring Boot offers many best practices including dependency management, configuration properties, and testing strategies.\",\n  \"source\": \"Dev Blog\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/content/share",
              "host": ["{{base_url}}"],
              "path": ["api", "content", "share"]
            }
          }
        },
        {
          "name": "5. Share Content - Different Topic",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"type\": \"ARTICLE\",\n  \"title\": \"Getting Started with React\",\n  \"description\": \"Learn React fundamentals\",\n  \"textContent\": \"React is a JavaScript library for building user interfaces. It uses components and virtual DOM.\",\n  \"source\": \"Frontend Blog\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/content/share",
              "host": ["{{base_url}}"],
              "path": ["api", "content", "share"]
            }
          }
        },
        {
          "name": "6. Get My Content",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "{{base_url}}/api/content/my-content",
              "host": ["{{base_url}}"],
              "path": ["api", "content", "my-content"]
            }
          }
        },
        {
          "name": "7. Get Content by Type - ARTICLE",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "{{base_url}}/api/content/my-content/type/ARTICLE",
              "host": ["{{base_url}}"],
              "path": ["api", "content", "my-content", "type", "ARTICLE"]
            }
          }
        },
        {
          "name": "8. Search Content",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "{{base_url}}/api/content/my-content/search?query=spring",
              "host": ["{{base_url}}"],
              "path": ["api", "content", "my-content", "search"],
              "query": [
                {
                  "key": "query",
                  "value": "spring"
                }
              ]
            }
          }
        }
      ]
    },
    {
      "name": "Memories",
      "item": [
        {
          "name": "9. Get All Memories",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "{{base_url}}/api/memories",
              "host": ["{{base_url}}"],
              "path": ["api", "memories"]
            }
          }
        },
        {
          "name": "10. Get Memory by ID",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "{{base_url}}/api/memories/1",
              "host": ["{{base_url}}"],
              "path": ["api", "memories", "1"]
            }
          }
        },
        {
          "name": "11. Search Memories",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "{{base_url}}/api/memories/search?query=spring",
              "host": ["{{base_url}}"],
              "path": ["api", "memories", "search"],
              "query": [
                {
                  "key": "query",
                  "value": "spring"
                }
              ]
            }
          }
        },
        {
          "name": "12. Get Memories by Context",
          "request": {
            "method": "GET",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "{{base_url}}/api/memories/context/spring",
              "host": ["{{base_url}}"],
              "path": ["api", "memories", "context", "spring"]
            }
          }
        },
        {
          "name": "13. Create Memory",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"context\": \"Python Programming\",\n  \"type\": \"Note\",\n  \"content\": \"Python is a versatile programming language\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/memories",
              "host": ["{{base_url}}"],
              "path": ["api", "memories"]
            }
          }
        },
        {
          "name": "14. Update Memory",
          "request": {
            "method": "PUT",
            "header": [
              {
                "key": "Content-Type",
                "value": "application/json"
              },
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"context\": \"Updated Spring Boot Context\",\n  \"content\": \"Updated content about Spring Boot\"\n}"
            },
            "url": {
              "raw": "{{base_url}}/api/memories/1",
              "host": ["{{base_url}}"],
              "path": ["api", "memories", "1"]
            }
          }
        },
        {
          "name": "15. Delete Memory",
          "request": {
            "method": "DELETE",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": {
              "raw": "{{base_url}}/api/memories/1",
              "host": ["{{base_url}}"],
              "path": ["api", "memories", "1"]
            }
          }
        }
      ]
    }
  ]
}
```

---

## Quick Setup Steps

### Step 1: Create Postman Collection File

1. Copy the JSON above
2. Save it as `MemoryBook.postman_collection.json`
3. In Postman: **Import** → **File** → Select the file

### Step 2: Create Environment

1. In Postman: **Environments** → **+**
2. Name: `MemoryBook Local`
3. Add variables:
   - `base_url`: `http://localhost:8080`
   - `token`: (leave empty)
4. **Save**
5. Select environment from dropdown (top right)

### Step 3: Test Flow

1. **Signup** → Create user
2. **Login** → Token auto-saves (check Tests script)
3. **Share Content** → Test the main feature
4. **Get Memories** → Verify memory creation/updates

---

## Tips

1. **Auto-save Token**: The Login request has a Tests script that saves the token automatically
2. **Token Expires**: Token expires in 5 minutes - login again if needed
3. **Check Response**: Look for `newMemory: true/false` to verify memory creation/update
4. **Verify Updates**: Check `relevanceScore` and `relatedContentIds` after sharing similar content

---

## Troubleshooting

### Token Not Saving
- Check if environment is selected (top right dropdown)
- Verify Tests script in Login request

### 401 Unauthorized
- Make sure token is in Authorization header: `Bearer {{token}}`
- Token might have expired (5 minutes) - login again
- Check token variable in environment

### 500 Error
- Check if application is running
- Verify database connection
- Check application logs

---

Happy Testing! 🚀

