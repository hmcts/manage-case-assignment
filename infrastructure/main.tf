provider "azurerm" {
  version = "1.44.0"
}

resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location}"

  tags = "${var.common_tags}"
}

module "key-vault" {
  source              = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  product             = "${var.product}"
  env                 = "${var.env}"
  tenant_id           = "${var.tenant_id}"
  object_id           = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${azurerm_resource_group.rg.name}"

  # dcd_cc-dev group object ID
  product_group_object_id    = "38f9dea6-e861-4a50-9e73-21e64f563537"
  common_tags                = "${var.common_tags}"
  managed_identity_object_id = "${var.managed_identity_object_id}"
}

resource "azurerm_key_vault_secret" "AZURE_APPINSGHTS_KEY" {
  name         = "AppInsightsInstrumentationKey"
  value        = "${azurerm_application_insights.appinsights.instrumentation_key}"
  key_vault_id = "${module.key-vault.key_vault_id}"
}

resource "azurerm_application_insights" "appinsights" {
  name                = "${var.product}-${var.env}"
  location            = "${var.location}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
  application_type    = "Web"

  tags = "${var.common_tags}"
}

data "azurerm_key_vault" "s2s_vault" {
  name = "s2s-${var.env}"
  resource_group_name = "rpe-service-auth-provider-${var.env}"
}
data "azurerm_key_vault_secret" "manage-case-s2s-vault-secret" {
  name = "microservicekey-aac-manage-case-assignment"
  key_vault_id = "${data.azurerm_key_vault.s2s_vault.id}"
}

resource "azurerm_key_vault_secret" "aac-manage-case-s2s-secret" {
  name = "aac-manage-case-s2s-secret"
  value = "${data.azurerm_key_vault_secret.manage-case-s2s-vault-secret.value}"
  key_vault_id = "${module.key-vault.key_vault_id}"
}

data "azurerm_key_vault" "notify_vault" {
  name = "notify-mca-api-key-${var.env}"
  resource_group_name = "${azurerm_resource_group.rg.name}"
}

data "azurerm_key_vault_secret" "notify-mca-api-key-secret" {
  name = "notify-mca-api-key"
  key_vault_id = "${data.azurerm_key_vault.notify_vault.id}"
}

resource "azurerm_key_vault_secret" "notify-mca-api-key" {
  name = "notify-mca-api-key"
  value = "${data.azurerm_key_vault_secret.notify-mca-api-key-secret.value}"
  key_vault_id = "${module.key-vault.key_vault_id}"
}
