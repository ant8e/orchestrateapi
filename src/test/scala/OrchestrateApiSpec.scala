import org.scalatest.FunSuite
import scala.concurrent.Await
import spray.httpx.unmarshalling.MalformedContent
import spray.json.DefaultJsonProtocol

/**
 *
 */
class OrchestrateApiSpec extends FunSuite {

  import net.antoinecomte.orchestrateapi._
  import scala.concurrent.duration._

  val connection = Client("5dd2be1b-f5e1-4002-a9b5-6acd8cf936f5")

  case class Fruit(name: String, taste: String)

  case class Meat(animal: String)

  object FruitJsonProtocol extends DefaultJsonProtocol {
    implicit val fruitFormat = jsonFormat2(Fruit)
    implicit val meatformat = jsonFormat1(Meat)
  }

  test("getKeyJson") {
    val key = connection.getKey("fruits", "apple").asJson
    val result = Await.result(key, 10.seconds)
    println(result)
  }

  test("getKey") {
    import FruitJsonProtocol._
    val key = connection.getKey("fruits", "apple").as[Fruit]
    val result = Await.result(key, 10.seconds)
    assert(result.isDefined)
    println(result)
  }

  test("getUnkownKey") {
    import FruitJsonProtocol._
    val key = connection.getKey("fruits", "apple42").as[Fruit]
    val result = Await.result(key, 10.seconds)
    println(result)
  }

  test("get Key with incorrect type") {
    import FruitJsonProtocol._
    intercept[ClientException] {
      Await.result(connection.getKey("fruits", "apple").as[Meat], 10.seconds)
    }
  }


  test("getKey with Ref") {
    import FruitJsonProtocol._
    val key = connection.getKey("fruits", "apple", Some("8953e887205d65e4")).as[Fruit]
    val result = Await.result(key, 10.seconds)
    println(result)
  }

  test("putKey") {
    import FruitJsonProtocol._
    val key = connection.putKey("fruits", "apple2", Fruit("a", "b"))
    val result: Int = Await.result(key, 10.seconds)
    assert(result == 201)
  }

  test("putKeyJson") {
    val key = connection.putKey("fruits", "apple3", """{
                                                        "name":"Apple",
                                                       "taste":"Good"
                                                       }
                                                      | """)
    val result: Int = Await.result(key, 10.seconds)
    assert(result == 201)
  }
}
