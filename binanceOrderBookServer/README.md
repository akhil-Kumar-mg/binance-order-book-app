# Binance-order-book-app

#### This application has been written using java Spring Boot.

##Requirements

1. Java 8 
2. Maven
2. This application uses Influx DB ( A time series database ) so make sure you have influxDB server running in your system.
   You can download the influxDB from here "https://portal.influxdata.com/downloads/"

#### There are six API

**GET** ``` /api/external-books?name=nameOfABook ```

For this API, the application queries the Ice And Fire API with the value in *nameOfABook* and use the data received to respond with the following JSON if there are results:
```
{
    "status_code": 200,
    "status": "success",
    "data": [
        {
            "name": "A Game of Thrones",
            "isbn": "978-0553103540",
            "authors": [
                "George R. R. Martin"
            ],
            "number_of_pages": 694,
            "publisher": "Bantam Books",
            "country": "United States",
            "release_date": "1996-08-01"
        }
    ]
}
```

or this JSON if the Ice and Fire API returns no results:
```
{
    "status_code": 200,
    "status": "success",
    "data": []
}
```



**POST** ``` /api/v1/books ```

For this API, a book is created in the database. The following fields are all required in JSON format in the request body -
* name
* isbn
* authors
* country
* number_of_pages
* publisher
* release_date

If any of the above is missing, the response will be
```
{
    "status_code": 400,
    "status": "Failed"
}
```
If the fields are all present, the book is added to db and response will be similar to the following
```
{
    "status_code": 201,
    "status": "success",
    "data": [
        { "book": {
            "name": "My First Book",
            "isbn": "123-3213243567",
            "authors": [
                "John Doe"
            ],
            "number_of_pages": 350,
            "publisher": "Acme Books",
            "country": "United States",
            "release_date": "2019-08-01"
        } }
    ]
}
```
**GET** ``` /api/v1/books ```

This API is used to get books from the database, it can have query params to filter the result. The params can be -
* name
* country
* publisher
* release_date

If there are books which match the filter, the response will be as follows
```
{
    "status_code": 200,
    "status": "success",
    "data": [
        {
            "id": 1,
            "name": "A Game of Thrones",
            "isbn": "978-0553103540",
            "authors": [
                "George R. R. Martin"
            ],
            "number_of_pages": 694,
            "publisher": "Bantam Books",
            "country": "United States",
            "release_date": "1996-08-01"
        },
        {
            "id": 2,
            "name": "A Clash of Kings",
            "isbn": "978-0553108033",
            "author": [
                "George R. R. Martin"
            ],
            "number_of_pages": 768,
            "publisher": "Bantam Books",
            "country": "United States",
            "release_date": "1999-02-02"
        }
    ]
}
```

or this JSON if the Books API returns no results:
```
{
    "status_code": 200,
    "status": "success",
    "data": []
}
```

**PATCH** ``` /api/v1/books/:id ```

This API is used to update a book's details. Any of the following fields can have values and will be set to the book corresponding to id passed in the url
* name
* isbn
* authors
* country
* number_of_pages
* publisher
* release_date

If the book is present in database, it is updated and the response will be as follows
```
{
    "status_code": 200,
    "status": "success",
    "message": "The book My First Book was updated successfully",
    "data": {
        "id": 1,
        "name": "My First Updated Book",
        "isbn": "123-3213243567",
        "authors": [
            "John Doe"
        ],
        "number_of_pages": 350,
        "publisher": "Acme Books Publishing",
        "country": "United States",
        "release_date": "2019-01-01"
    }
}
```
If the book was missing, the following response is given
```
{
    "status_code": 404,
    "status": "failed",
    "message": "Book does not exist"
}
```

**DELETE** ``` /api/v1/books/:id ```

This API is used to delete a book from the database. It removes the book corresponding to the id in the url

After deleting the book, the response is as follows
```
{
    "status_code": 200,
    "status": "success",
    "message": "The book My First Book was deleted successfully",
    "data": []
}
```

If the book was missing, the following response is given
```
{
    "status_code": 404,
    "status": "failed",
    "message": "Book does not exist"
}
```

**GET** ``` /api/v1/books/:id ```
This API is used to fetch a book corresponding to the id in the url from the database.

If the book is present, the response is as follows
```
{
    "status_code": 200,
    "status": "success",
    "data": {
        "id": 1,
        "name": "My First Book",
        "isbn": "123-3213243567",
        "authors": [
            "John Doe"
        ],
        "number_of_pages": 350,
        "publisher": "Acme Books Publishing",
        "country": "United States",
        "release_date": "2019-01-01"
    }
}
```
