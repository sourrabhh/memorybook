# MemoryBook - Enhanced Features

## Overview
MemoryBook now supports intelligent content sharing with automatic context extraction, memory matching, and smart memory updates. When users share articles, blogs, news, or links, the system automatically:

1. **Extracts context** from the shared content
2. **Finds similar memories** using keyword matching and similarity algorithms
3. **Updates existing memories** or **creates new ones** automatically
4. **Generates suggestions and summaries** based on the context

---

## New Components

### 1. Content Model (`Content.java`)
Stores shared content (articles, blogs, news, links, images, videos):
- `ContentType`: ARTICLE, BLOG, NEWS, LINK, IMAGE, VIDEO
- Stores title, description, text content, URL, source
- Automatically extracts and stores context and keywords

### 2. Enhanced Memory Model (`Memory.java`)
Extended with new fields:
- `summary`: AI-generated summary
- `suggestions`: Context-based suggestions
- `relatedContentIds`: IDs of related shared content
- `relevanceScore`: How many times related content was shared
- `keywords`: Extracted keywords for matching
- `lastRelatedContentAt`: Timestamp of last related content

### 3. Services

#### ContextExtractionService
- Extracts context from content using keyword analysis
- Calculates similarity between contexts (Jaccard similarity)
- Generates summaries from text content

#### MemoryMatchingService
- Finds similar memories based on context similarity
- Uses a 30% similarity threshold by default
- Returns memories sorted by relevance and similarity

#### SuggestionService
- Generates suggestions based on memory context
- Creates summaries combining existing and new content

#### ContentService
- Manages content CRUD operations
- Supports searching and filtering by type

#### Enhanced MemoryService
- `createOrUpdateMemoryFromContent()`: Main method for smart memory creation/update
- `updateMemoryWithContent()`: Updates existing memory with new related content
- Automatically merges contexts, keywords, and content

---

## API Endpoints

### Content Endpoints (`/api/content`)

#### Share Content
```http
POST /api/content/share
Authorization: Bearer <token>
Content-Type: application/json

{
  "type": "ARTICLE",
  "title": "Introduction to Spring Boot",
  "description": "A comprehensive guide to Spring Boot",
  "textContent": "Spring Boot is a framework...",
  "url": "https://example.com/article",
  "source": "Tech Blog"
}
```

**Response:**
```json
{
  "contentId": 1,
  "memory": {
    "id": 1,
    "context": "spring, boot, framework, java",
    "summary": "Article about Spring Boot framework...",
    "suggestions": "This topic has been referenced 2 times...",
    "relevanceScore": 2,
    "relatedContentIds": "1,3"
  },
  "newMemory": false,
  "extractedContext": "spring, boot, framework, java",
  "suggestions": "...",
  "summary": "..."
}
```

#### Get My Content
```http
GET /api/content/my-content
Authorization: Bearer <token>
```

#### Get Content by Type
```http
GET /api/content/my-content/type/ARTICLE
Authorization: Bearer <token>
```

#### Search Content
```http
GET /api/content/my-content/search?query=spring
Authorization: Bearer <token>
```

### Memory Endpoints (`/api/memories`)

#### Get All Memories
```http
GET /api/memories
Authorization: Bearer <token>
```

#### Get Memory by ID
```http
GET /api/memories/{id}
Authorization: Bearer <token>
```

#### Search Memories
```http
GET /api/memories/search?query=spring
Authorization: Bearer <token>
```

#### Get Memories by Context
```http
GET /api/memories/context/spring
Authorization: Bearer <token>
```

#### Create Memory Manually
```http
POST /api/memories
Authorization: Bearer <token>
Content-Type: application/json

{
  "context": "Spring Boot",
  "type": "Note",
  "content": "My notes about Spring Boot"
}
```

#### Update Memory
```http
PUT /api/memories/{id}
Authorization: Bearer <token>
Content-Type: application/json

{
  "context": "Updated context",
  "content": "Updated content"
}
```

#### Delete Memory
```http
DELETE /api/memories/{id}
Authorization: Bearer <token>
```

---

## How It Works

### 1. Content Sharing Flow

1. **User shares content** via `/api/content/share`
2. **Context Extraction**: System extracts keywords and context from title, description, and text
3. **Similarity Matching**: System searches for existing memories with similar context (≥30% similarity)
4. **Smart Update/Create**:
   - If similar memory found → Update existing memory with new content
   - If no similar memory → Create new memory
5. **Enrichment**: Generate summary and suggestions
6. **Response**: Return memory with context, suggestions, and summary

### 2. Memory Update Logic

When updating an existing memory:
- Merges new content with existing content (separated by `---`)
- Merges contexts (comma-separated)
- Merges keywords (removes duplicates)
- Increments relevance score
- Updates timestamp
- Regenerates summary and suggestions

### 3. Similarity Algorithm

Uses **Jaccard Similarity**:
- Extracts keywords from both contexts
- Calculates intersection / union
- Returns similarity score (0.0 to 1.0)
- Threshold: 0.3 (30% similarity)

---

## Example Workflow

1. **User shares Article 1**: "Introduction to Spring Boot"
   - System creates Memory A with context: "spring, boot, framework"
   - Relevance Score: 1

2. **User shares Article 2**: "Spring Boot Best Practices"
   - System finds Memory A (similar context: "spring, boot")
   - Updates Memory A with Article 2 content
   - Relevance Score: 2
   - Related Content IDs: "1,2"
   - Suggestions: "This topic has been referenced 2 times..."

3. **User shares Article 3**: "Java Programming Basics"
   - No similar memory found
   - System creates Memory B with context: "java, programming, basics"
   - Relevance Score: 1

---

## Future Enhancements

### Planned Features:
- **AI Integration**: Replace simple keyword extraction with AI-based context extraction
- **Image/Video Processing**: Extract context from images and videos
- **Advanced Summarization**: Use AI models for better summaries
- **Content Recommendations**: Suggest related content based on memories
- **Memory Clustering**: Group related memories automatically
- **Timeline View**: Show memory evolution over time

### Integration Options:
- **OpenAI API**: For context extraction and summarization
- **Google Cloud NLP**: For entity recognition and sentiment analysis
- **Azure Cognitive Services**: For content understanding

---

## Database Schema Updates

### New Table: `contents`
- `id` (PK)
- `user_id`
- `type` (ENUM: ARTICLE, BLOG, NEWS, LINK, IMAGE, VIDEO)
- `title`, `description`, `text_content`, `url`, `source`
- `extracted_context`, `keywords`
- `created_at`, `updated_at`

### Updated Table: `memories`
- Added: `summary`, `suggestions`, `related_content_ids`
- Added: `relevance_score`, `keywords`, `last_related_content_at`
- `context` changed to TEXT type

---

## Usage Examples

### Share an Article
```bash
curl -X POST http://localhost:8080/api/content/share \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "type": "ARTICLE",
    "title": "Understanding REST APIs",
    "description": "A guide to RESTful API design",
    "textContent": "REST APIs are stateless...",
    "source": "Tech Blog"
  }'
```

### Get All My Memories
```bash
curl -X GET http://localhost:8080/api/memories \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Search Memories
```bash
curl -X GET "http://localhost:8080/api/memories/search?query=spring" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## Notes

- **Similarity Threshold**: Currently set to 0.3 (30%). Adjust in `MemoryMatchingService.SIMILARITY_THRESHOLD`
- **Context Extraction**: Currently uses simple keyword extraction. Can be enhanced with AI services.
- **Summary Length**: Default is 200 characters. Adjust in `ContextExtractionService.generateSummary()`
- **Authorization**: All endpoints require JWT authentication except `/api/auth/signup` and `/api/auth/login`

---

**Version**: 2.0.0
**Last Updated**: 2024

