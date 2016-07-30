package controllers

import java.util.UUID
import javax.inject.Inject

import config.ConfigBanana
import models.{Validation, Verification, VerificationDump}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsError, JsNull, Json}
import play.api.libs.ws.{WSClient, WSRequest}
import play.api.mvc.{Action, BodyParsers, Controller}
import services.{SimpleValidator, _}

import scala.concurrent.Future

class Verifications @Inject() (
                                val verificationRepo: VerificationMongoRepo,
                                val userRepo: UserMongoRepo,
                                val validator: SimpleValidator,
                                val messagesApi: MessagesApi,
                                val ws: WSClient,
                                val configuration: play.api.Configuration)
  extends Controller with I18nSupport with ConfigBanana {

  def processVerificationPost() = Action.async(BodyParsers.parse.json) {
    request =>
    val verification = request.body.validate[Verification]
    verification.fold(
      errors => {
        Future(BadRequest(
          Json.obj("status" -> "KO", "message" -> JsError.toJson(errors))))
      },
      verification => {
        val estimation = for {
          veri <- verificationRepo.save(verification)
          est <- validator.process(veri)
          throwaway <- dumpVerification(veri, est)
        } yield est
        estimation map {
          case a: Double => verification.value match {
            case Some(b) =>
              val validation = verification.value.get && a > 0.5
              addValidation(
                verification.verifier,
                Validation(verification.task_id.toString, verification.value.get))
              Ok(Json.toJson(validation))
            case None => Ok(JsNull)
          }
          case _ => Ok(Json.toJson("Error validation"))
        }
      }
    )
  }

  def addValidation(userId: UUID, validation: Validation) = {
    userRepo.findById(userId) flatMap {
      case Some(u) => userRepo.save(
        u.copy(validations = validation :: u.validations))
    }
  }

  def dumpVerification(verification: Verification, estimation: Double) =  {
    val url = configuration.getString("triplestore.uri").get
    val request: WSRequest = ws.url(url)
    import ops._
    val test = for {
      link <- verificationRepo.getLink(verification)
    } yield turtleWriter.asString(
      VerificationDump(
        verification._id,
        verification.verifier.toString,
        link.copy(estimation = Some(estimation)),
        verification.value
      ).toPG.graph, "").get
    for {
      test <- test
      req <- request.withHeaders(
        "Content-Type" -> "text/turtle").withMethod("POST").post(test)
    } yield "Done"
  }
}
