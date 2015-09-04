package controllers

import com.amazonaws.auth.{AWSCredentials, BasicAWSCredentials}
import com.gu.pandomainauth.action.AuthActions
import com.gu.pandomainauth.model.AuthenticatedUser
import services.BlottrConfig

trait PanDomainAuthActions extends AuthActions {

  import play.api.Play.current
  lazy val config = play.api.Play.configuration

  override def validateUser(authedUser: AuthenticatedUser): Boolean = {
    (authedUser.user.email endsWith ("@guardian.co.uk")) && authedUser.multiFactor
  }

  override def cacheValidation = true

  override def authCallbackUrl: String = "/unused"

  override lazy val domain: String = BlottrConfig().pandaDomain

  override lazy val system: String = "blottr"
}
