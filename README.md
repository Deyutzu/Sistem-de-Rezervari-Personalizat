# StayFinder

Platformă de descoperire și rezervare de cazări cu **feed personalizat** în stil social
(carduri, favorite, recenzii). Un singur backend **Spring Boot** servește atât aplicația
**Android** (API REST + JWT), cât și **interfața web** (Thymeleaf).

> Proiect pentru disciplinele *Calcul mobil* și *Proiectarea aplicațiilor web*.

## Stadiu

| Fază | Conținut | Stare |
|------|----------|-------|
| 1 | Backend de bază: entități, import JSON, GET cazări, Swagger | ✅ |
| 2 | Android: listă + detaliu, navigare | ⏳ |
| 3 | Android: Room, onboarding | ⏳ |
| 4 | Backend: JWT, preferințe, favorite, rezervări, recomandări | ⏳ |
| 5 | Android ↔ backend (Retrofit), feed personalizat | ⏳ |
| 6 | Web Thymeleaf + panou admin | ⏳ |
| 7 | Finisaje + funcționalitate avansată | ⏳ |

## Funcționalități (faza 1)

- 60 de cazări reale/plauzibile (munte, mare, oraș) importate automat la pornire
- Tabel `SimilarRegion` pre-populat (ex. Dolomiți IT ↔ Dolomiții Lienz AT)
- Căutare cu filtre combinabile, sortare și paginare
- Format JSON uniform pentru erori
- Documentație interactivă Swagger UI

## API (faza 1)

| Metodă | Endpoint | Descriere |
|--------|----------|-----------|
| GET | `/api/accommodations` | Listă paginată. Filtre opționale: `type` (SEA/MOUNTAIN/CITY), `minPrice`, `maxPrice`, `allInclusive`, `country`, `search`; `sort` (RATING / PRICE_ASC / PRICE_DESC), `page`, `size` |
| GET | `/api/accommodations/{id}` | Detaliile unei cazări |

Exemplu: `GET /api/accommodations?type=MOUNTAIN&country=Austria&maxPrice=200&page=0&size=10`

## Rulare backend

Cerințe: **JDK 17+** (Maven nu trebuie instalat — proiectul include Maven Wrapper).

```powershell
cd backend
.\mvnw.cmd spring-boot:run        # Windows
# ./mvnw spring-boot:run          # Linux / macOS
```

- Swagger UI: <http://localhost:8080/swagger-ui.html>
- Consola H2: <http://localhost:8080/h2-console> (JDBC URL `jdbc:h2:mem:stayfinder`, user `sa`, fără parolă)
- Teste: `.\mvnw.cmd test`

### PostgreSQL (opțional)

1. Copiază `backend/src/main/resources/application-local.properties.example` în
   `application-local.properties` (ignorat de git) și completează `DB_PASSWORD`
   — sau setează variabilele de mediu `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`.
2. Rulează cu profilul `postgres`:
   ```powershell
   .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=postgres"
   ```

## Structura proiectului

```
pawcmo/
├── backend/                         Spring Boot (Maven)
│   └── src/main/java/com/stayfinder/
│       ├── config/                  DataSeeder (import JSON), OpenApiConfig
│       ├── controller/              REST controllers (/api/...)
│       ├── dto/                     Obiecte expuse prin API (nu entitățile)
│       ├── exception/               GlobalExceptionHandler + excepții proprii
│       ├── model/                   Entități JPA + enum-uri
│       ├── repository/              Spring Data JPA + Specifications (filtre)
│       └── service/                 Logica de business
│   └── src/main/resources/
│       ├── application.properties           H2 (dev)
│       ├── application-postgres.properties  profil PostgreSQL
│       └── data/                            accommodations.json, similar-regions.json
└── android/                         Aplicația Android (din faza 2)
```

## Securitate

Nicio parolă sau cheie nu este pusă în repository. Secretele se citesc din
`application-local.properties` / `local.properties` (ambele în `.gitignore`)
sau din variabile de mediu. Vezi fișierele `*.example`.
