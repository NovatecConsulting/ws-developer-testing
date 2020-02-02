from library.books import *

data_store = BookDataStore()
id_generator = BookIdGenerator(data_store)
event_dispatcher = EventDispatcher()

book_collection = BookCollection(id_generator, data_store, event_dispatcher)

book = Book(title="Clean Code", isbn="84792837543")
newRecord = book_collection.add_book(book)
foundRecord = book_collection.get_book(newRecord.id)

print(book)
print(newRecord)
print(foundRecord)