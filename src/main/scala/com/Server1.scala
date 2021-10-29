package com
/*
import hello.{GreeterGrpc, HelloReply, HelloRequest}
import io.grpc.netty.NettyServerBuilder
import io.grpc.{Server, ServerBuilder}

import scala.concurrent.{ExecutionContext, Future}

object Server1  {
  def main(args: Array[String]):Unit = {
    val server = new Server1(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }
}

class Server1(executionContext: ExecutionContext) {
  private class GreeterImpl extends GreeterGrpc.Greeter {
    override def sayHello(request: HelloRequest): Future[HelloReply] = {
//      val hash = log_process1.start(request.time, request.interval)

      val reply = HelloReply("hash")
      Future.successful(reply)
    }
  }

  var server: Server = null

  def start() = {
    val serverBuilder = NettyServerBuilder.forPort(55555)
    serverBuilder.addService(GreeterGrpc.bindService(new GreeterImpl, executionContext))
    server = serverBuilder.build().start()
    sys.addShutdownHook {
      if (server != null)
        server.shutdown()
    }
  }

  def blockUntilShutdown() = {
    if (server != null)
      server.awaitTermination()
  }

}
*/