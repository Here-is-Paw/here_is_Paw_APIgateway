terraform {
  required_providers {
    ncloud = {
      source  = "NaverCloudPlatform/ncloud"
      version = "3.3.0"
    }
  }
}

provider "ncloud" {
  support_vpc = true
  access_key  = var.access_key
  secret_key  = var.secret_key
  region      = var.region
}

# VPC 생성
resource "ncloud_vpc" "vpc_1" {
  name            = "${var.prefix}-vpc"
  ipv4_cidr_block = "10.0.0.0/16"
}

# 서브넷 생성
resource "ncloud_subnet" "subnet_1" {
  vpc_no          = ncloud_vpc.vpc_1.vpc_no
  name            = "${var.prefix}-subnet"
  subnet          = "10.0.1.0/24"
  zone            = var.zone
  network_acl_no  = ncloud_vpc.vpc_1.default_network_acl_no
  subnet_type     = "PUBLIC"
}

# ACG 생성
resource "ncloud_access_control_group" "sg_1" {
  name   = "${var.prefix}-sg"
  vpc_no = ncloud_vpc.vpc_1.vpc_no
}

# ACG 규칙 추가
resource "ncloud_access_control_group_rule" "sg_rules" {
  access_control_group_no = ncloud_access_control_group.sg_1.id

  inbound {
    protocol    = "TCP"
    ip_block    = "0.0.0.0/0"
    port_range  = "22"
    description = "SSH Access"
  }

  inbound {
    protocol    = "TCP"
    ip_block    = "0.0.0.0/0"
    port_range  = "8000-9000"
    description = "Spring Application Access"
  }

  inbound {
    protocol    = "TCP"
    ip_block    = "0.0.0.0/0"
    port_range  = "80-81"
    description = "Kibana Access"
  }

  outbound {
    protocol    = "TCP"
    ip_block    = "0.0.0.0/0"
    port_range  = "1-65535"
    description = "Allow All TCP Outbound"
  }
}

#NIC 생성
resource "ncloud_network_interface" "nic_1" {
  name = "${var.prefix}-nic"
  subnet_no = ncloud_subnet.subnet_1.id
  access_control_groups = [ncloud_access_control_group.sg_1.id]
}


# 서버 생성
resource "ncloud_server" "server_1" {
  subnet_no                 = ncloud_subnet.subnet_1.id
  name                      = "${var.prefix}-server"
  server_image_product_code = var.server_image_product_code
  server_product_code       = var.server_product_code
  login_key_name            = var.login_key_name
  init_script_no            = ncloud_init_script.init.id

  network_interface   {
    network_interface_no = ncloud_network_interface.nic_1.id
    order = 0
  }

  depends_on = [
    ncloud_access_control_group_rule.sg_rules
  ]
}

# 퍼블릭 IP 생성
resource "ncloud_public_ip" "public_ip_1" {
  server_instance_no = ncloud_server.server_1.id
}