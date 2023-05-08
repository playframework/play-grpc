/*
 * Copyright (C) from 2022 The Play Framework Contributors <https://github.com/playframework>, 2011-2021 Lightbend Inc. <https://www.lightbend.com>
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
