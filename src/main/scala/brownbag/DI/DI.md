# what is dependency injection?

Without dependency injection:

class Foo {
  def bar = {
    val db = new DB
    val x = db.get
    val y = ???
    db.store(y)
  }
}

With dependency injection:

class Foo(db: DB) {
  def bar = {
    val x = db.get
    val y = ???
    db.store(y)
  }
}




# Why?

 * testing
 * loose coupling
 * tracking effects

# What are we doing in collaborate-api

 some cake, some constructor injection...

 cake: API, PAN integration
 constructor: most other places

 # Plan for secure messaging and other places

  * constructor or method tagless final style or normal implicits

  

