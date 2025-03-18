package controllers

import javax.inject._
import play.api.mvc._
import play.api.libs.ws.WSClient
import play.api.libs.json._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import dtos.{TitleRequest, TitleResponse}

@Singleton
class TitleController @Inject()(ws: WSClient, val controllerComponents: ControllerComponents) extends BaseController {

    implicit val titleRequestFormat: OFormat[TitleRequest] = Json.format[TitleRequest]
    implicit val titleResponseFormat: OFormat[TitleResponse] = Json.format[TitleResponse]

    def getTitles: Action[JsValue] = Action.async(parse.json) { implicit request =>
        request.body.validate[TitleRequest] match {
            case JsSuccess(titleRequest, _) =>
                val urls = titleRequest.urls
                if (urls.isEmpty) {
                    Future.successful(BadRequest("No URLs provided"))
                } else {
                    val futures = urls.distinct.map { url =>
                        val fullUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) s"http://$url" else url
                        ws.url(fullUrl).get().map { response =>
                            val title = extractTitle(response.body)
                            TitleResponse(fullUrl, title)
                        }.recover {
                            case _: Exception => TitleResponse(url, "Error: Unable to fetch title")
                        }
                    }
                    Future.sequence(futures).map { results =>
                        Ok(Json.toJson(results))
                    }
                }
            case JsError(errors) =>
                Future.successful(BadRequest(s"Invalid request body: $errors"))
        }
    }

    private def extractTitle(html: String): String = {
        val titlePattern = "<title>(.*?)</title>".r
        titlePattern.findFirstMatchIn(html) match {
            case Some(m) => m.group(1)
            case None => "No title found"
        }
    }
}