# Compliq Deployment Walkthrough

I have generated all the necessary configuration files for your production-grade deployment! Since this is a learning project running entirely on AWS Free Tier, we are optimizing for cost by running Jenkins, MySQL, Backend, Frontend, and Nginx all on a single EC2 instance.

## Files Created

- [compliq/Dockerfile](file:///b:/placement%20course/JAVA%20development/compliqMain/compliq/Dockerfile): Multi-stage build for compiling and packaging the Java Spring Boot backend (includes test execution).
- [frontend/Dockerfile](file:///b:/placement%20course/JAVA%20development/compliqMain/frontend/Dockerfile): Multi-stage build for Vite frontend with an Nginx container serving static files.
- [docker-compose.yml](file:///b:/placement%20course/JAVA%20development/compliqMain/docker-compose.yml): Orchestrates Jenkins, MySQL, Backend, Frontend, and the main Nginx Reverse Proxy.
- [nginx/nginx.conf](file:///b:/placement%20course/JAVA%20development/compliqMain/nginx/nginx.conf): Routes `/api` to the backend and `/` to the frontend.
- [Jenkinsfile](file:///b:/placement%20course/JAVA%20development/compliqMain/Jenkinsfile): The CI/CD pipeline that builds and redeploys the Docker containers upon a code push.

## Next Steps: AWS EC2 Setup

Follow these manual steps to provision your infrastructure and deploy.

### 1. Launch an EC2 Instance
1. Go to the AWS Console -> **EC2** -> **Launch Instances**.
2. Select **Ubuntu Server 22.04 LTS**.
3. Choose **t2.micro** or **t3.micro** (Free Tier eligible).
4. Create a new key pair (e.g., `compliq-key.pem`) and download it.
5. In **Network Settings**, allow **SSH (port 22)**, **HTTP (port 80)**, and **Custom TCP (port 8081)** (for Jenkins) from anywhere.
6. Launch the instance.

> [!WARNING]
> Running all these containers on a `t2.micro` (1GB RAM) will be very tight on memory. If the instance freezes or crashes due to Out-Of-Memory (OOM), you may need to [add a swap file](https://linuxize.com/post/create-a-linux-swap-file/) (e.g., 2GB swap) on your Ubuntu server to prevent MySQL or Java from crashing.

### 2. Install Docker and Docker Compose
SSH into your instance:
```bash
ssh -i "compliq-key.pem" ubuntu@<YOUR-EC2-PUBLIC-IP>
```

Run the following commands to install Docker:
```bash
sudo apt update
sudo apt install docker.io docker-compose-v2 git -y
sudo usermod -aG docker ubuntu
```
*Note: You may need to log out and log back in for the `docker` group changes to take effect.*

### 3. Start Jenkins and MySQL
Clone your repository onto the EC2 instance (you can also just manually copy `docker-compose.yml` if your repo is private and you don't have keys set up yet).
```bash
git clone https://github.com/SmitPawar21/Compliq.git
cd Compliq
```

Start the initial containers:
```bash
# Start Jenkins and MySQL in the background
docker compose up -d jenkins mysql
```

### 4. Configure Jenkins
1. Open your browser and go to `http://<YOUR-EC2-PUBLIC-IP>:8081`.
2. Retrieve the initial admin password:
   ```bash
   docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword
   ```
3. Install suggested plugins and create your admin user.
4. **Create the Pipeline**:
   - Click **New Item** -> Name it "Compliq-Pipeline" -> Select **Pipeline**.
   - Under Pipeline definition, choose **Pipeline script from SCM**.
   - Select **Git**, provide your GitHub repository URL.
   - Specify the branch to build (`*/main`).
   - Save.

> [!TIP]
> **GitHub Webhooks**: To trigger Jenkins automatically on push, go to your GitHub repository -> Settings -> Webhooks. Add a webhook:
> - **Payload URL**: `http://<YOUR-EC2-PUBLIC-IP>:8081/github-webhook/`
> - **Content type**: `application/json`
> In your Jenkins Pipeline configuration, check "GitHub hook trigger for GITScm polling".

### 5. Run the Pipeline!
Click **Build Now** in Jenkins.
The pipeline will:
1. Check out your code.
2. Build the Docker images (which runs your tests internally).
3. Deploy the updated `backend`, `frontend`, and `nginx` containers.

Once completed, you can access your application at `http://<YOUR-EC2-PUBLIC-IP>`.
