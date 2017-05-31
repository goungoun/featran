/*
 * Copyright 2017 Spotify AB.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.spotify.featran.transformers

import org.scalacheck._

object MinMaxScalerSpec extends TransformerProp("MinMaxScaler") {

  property("default") = Prop.forAll { xs: List[Double] =>
    test(xs, 0.0, 1.0)
  }

  // limit the range of min and max to avoid overflow
  private val minMaxGen = for {
    min <- Gen.choose(-1000.0, 1000.0)
    range <- Gen.choose(1.0, 2000.0)
  } yield (min, min + range)

  property("params") = Prop.forAll(list[Double].arbitrary, minMaxGen) { (xs, p) =>
    val (minP, maxP) = p
    test(xs, minP, maxP)
  }

  private def test(xs: List[Double], minP: Double, maxP: Double): Prop = {
    val (min, max) = (xs.min, xs.max)
    val delta = max - min
    val expected = xs.map(x => Seq((x - min) / delta * (maxP - minP) + minP))
    val oob = List((min - 1, Seq(minP)), (max + 1, Seq(maxP))) // out of lower and upper bounds
    test(MinMaxScaler("min_max", minP, maxP), xs, Seq("min_max"), expected, Seq(minP), oob)
  }

}
