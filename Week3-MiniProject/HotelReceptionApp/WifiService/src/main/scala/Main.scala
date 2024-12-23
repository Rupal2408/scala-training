import actors.MailSenderActorSystem.wifiMailSender
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import models.{Email, GuestInfo}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import models.JsonFormats._
import spray.json._

object Main {
  private def compose(guestInfo: GuestInfo): Email = {
    val body: String = s"Welcome to Hotel Grand, ${guestInfo.name},\n\nPlease find the Wifi credentials here.\nname:  HtGrand224\npassword: DFG123@333\n\nBest Regards,\nHotel Grand"
    Email(guestInfo.email, "Wifi Details", body)
  }
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "HotelRoomServiceNotification")

    val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(sys.env.get("BROKER_HOST").getOrElse("localhost")+":9092")
      .withGroupId("wifiServiceGroup")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

    Consumer.plainSource(consumerSettings, Subscriptions.topics("reservation-topic"))
      .map(record => {
        val guest = record.value().parseJson.convertTo[GuestInfo]
        compose(guest)
      }) // Convert JSON string to Person
      .runWith(Sink.actorRef(wifiMailSender,
        onCompleteMessage = s"Wifi credentials sent to guest",
        onFailureMessage = throwable => s"Error occured: ${throwable.getMessage}"
      ))
  }
}