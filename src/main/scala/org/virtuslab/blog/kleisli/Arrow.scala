package org.virtuslab.blog.kleisli

import scala.language.{ higherKinds, implicitConversions }

trait Arrow[=>:[_, _]] {
  def id[A]: A =>: A
  def arr[A, B](f: A => B): A =>: B

  def compose[A, B, C](fbc: B =>: C, fab: A =>: B): A =>: C

  def first[A, B, C](f: A =>: B): (A, C) =>: (B, C)
  def second[A, B, C](f: A =>: B): (C, A) =>: (C, B)

  def merge[A, B, C, D](f: A =>: B, g: C =>: D): (A, C) =>: (B, D) = compose(first(f), second(g))
  def split[A, B, C](fab: A =>: B, fac: A =>: C): A =>: (B, C) = compose(merge(fab, fac), arr((x: A) => (x, x)))

  def tee[A, B](f: A =>: B, action: A => Unit): A =>: B = compose(arr((x: (B, Unit)) => x._1), split(f, arr(action)))
}

final class ArrowOps[=>:[_, _], A, B](val self: A =>: B)(implicit val arr: Arrow[=>:]) {
  def >>>[C](fbc: B =>: C): A =>: C = arr.compose(fbc, self)

  def <<<[C](fca: C =>: A): C =>: B = arr.compose(self, fca)

  def ***[C, D](g: C =>: D): (A, C) =>: (B, D) = arr.merge(self, g)

  def &&&[C](fac: A =>: C): A =>: (B, C) = arr.split(self, fac)

  def -|(action: B => Unit): A =>: B = >>>(arr.tee(arr.id, action))

  def lift[F[_[_, _], _, _]](implicit t: ArrowTransformer[F]): F[=>:, A, B] = t.lift[=>:, A, B](self)
}

object Arrow extends ArrowInstances {
  implicit def ToArrowOps[F[_, _], A, B](v: F[A, B])(implicit arr: Arrow[F]): ArrowOps[F, A, B] = new ArrowOps(v)
  implicit def ToArrowOpsFromKleisliLike[G[_], F[_[_], _, _], A, B](v: F[G, A, B])(implicit arr: Arrow[F[G, ?, ?]]) =
    new ArrowOps[F[G, ?, ?], A, B](v)
  implicit def ToArrowOpsFromReaderTLike[G[_, _], F[_, _[_, _], _, _], A, B, C](v: F[A, G, B, C])(implicit arr: Arrow[F[A, G, ?, ?]]) =
    new ArrowOps[F[A, G, ?, ?], B, C](v)
}
