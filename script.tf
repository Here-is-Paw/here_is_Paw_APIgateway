# Init script 생성
resource "ncloud_init_script" "init" {
  name    = "${var.prefix}-init"
  content = <<-EOF
              #!/bin/bash

              # Setup logging
              exec 1> >(tee -a "/var/log/user-data.log") 2>&1

              echo "[INFO] Starting installation..."

              # Update system and install Docker
              echo "[INFO] Installing Docker..."
              apt-get update
              apt-get install -y docker.io

              # Start and enable Docker
              echo "[INFO] Starting Docker service..."
              systemctl start docker
              systemctl enable docker
              sleep 10

              # Install Docker Compose
              echo "[INFO] Installing Docker Compose..."
              apt-get install -y docker-compose-plugin
              apt-get install -y docker-compose

              EOF
}