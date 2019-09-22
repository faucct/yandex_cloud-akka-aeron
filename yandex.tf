variable "yandex_cloud_id" {}
variable "yandex_folder_id" {}
variable "yandex_token" {}

provider "yandex" {
  cloud_id = var.yandex_cloud_id
  folder_id = var.yandex_folder_id
  token = var.yandex_token
  zone = "ru-central1-a"
}

resource "yandex_vpc_network" "default" {
}

resource "yandex_vpc_subnet" "default" {
  network_id = yandex_vpc_network.default.id
  v4_cidr_blocks = [
    "10.0.0.0/24"
  ]
}

resource "yandex_compute_instance" "default" {
  count = 2
  boot_disk {
    initialize_params {
      image_id = "fd87va5cc00gaq2f5qfb"
    }
  }

  resources {
    cores = 1
    memory = 2
  }
  metadata = {
    ssh-keys = "ubuntu:${file("~/.ssh/id_rsa.pub")}"
  }
  network_interface {
    subnet_id = yandex_vpc_subnet.default.id
    nat = true
  }
}

output "yandex_public_ips" {
  value = yandex_compute_instance.default.*.network_interface.0.nat_ip_address
}

output "yandex_private_ips" {
  value = yandex_compute_instance.default.*.network_interface.0.ip_address
}
