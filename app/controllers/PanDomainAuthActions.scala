package controllers

import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}
import com.gu.pandomainauth.action.AuthActions
import com.gu.pandomainauth.model.AuthenticatedUser

trait PanDomainAuthActions extends AuthActions {

  import play.api.Play.current
  lazy val config = play.api.Play.configuration

  override def validateUser(authedUser: AuthenticatedUser): Boolean = {
    (authedUser.user.email endsWith ("@guardian.co.uk")) && authedUser.multiFactor
  }

  override def cacheValidation = true

  override def authCallbackUrl: String = "/unused"

  override lazy val domain: String = config.getString("pandomain.domain").getOrElse("local.dev-gutools.co.uk")

  lazy val awsSecretAccessKey = config.getString("pandomain.aws.secret")
  lazy val awsKeyId = config.getString("pandomain.aws.keyId")

  override lazy val awsCredentials: Option[AWSCredentials] =
    for (key <- awsKeyId; secret <- awsSecretAccessKey)
      yield new BasicAWSCredentials(key, secret)

  override lazy val system: String = "workflow"
}
