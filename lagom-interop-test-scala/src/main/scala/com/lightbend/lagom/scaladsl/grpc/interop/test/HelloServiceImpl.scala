/*
 * Copyright (C) 2019 Lightbend Inc. <https://www.lightbend.com>
 */
package com.lightbend.lagom.scaladsl.grpc.interop.test

import com.lightbend.lagom.scaladsl.api.ServiceCall

import scala.concurrent.Future

/**
 * Implementation of the HelloService.
 */
class HelloServiceImpl() extends HelloService {

  override def hello(id: String) = ServiceCall { _ =>
    Future.successful(s"Hi $id!")
  }
}
