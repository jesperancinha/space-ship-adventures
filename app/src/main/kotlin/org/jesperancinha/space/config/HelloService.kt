package org.jesperancinha.space.config

fun interface HelloService {
    fun sayHello()
}
fun interface AnotherHelloService : HelloService{
    override fun sayHello()
}

class HelloServiceImpl : AnotherHelloService {
    override fun sayHello() {
        println("Hello! I'm concrete!")
    }
}