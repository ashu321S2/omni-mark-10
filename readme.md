🚀 The Full-Stack Odyssey: Deploying a Scalable Spring Boot App on AKS with DuckDNS & Let’s Encrypt


Building an application is only half the battle—the real magic begins when you move it from localhost:8080 to a globally accessible, secure, production-ready environment.
This project was a deep dive into the modern cloud-native ecosystem, combining backend development, containerization, orchestration, and real-world DevOps problem-solving. It was as much a journey through infrastructure as it was through code.


🧩 Project Overview

The journey began with Spring Boot, where I developed a robust REST API to handle all blog-related logic. To keep deployment simple and efficient, the frontend assets were embedded directly into:

src/main/resources/static

This approach allowed the entire application (backend + frontend) to be packaged into a single, portable JAR file—easy to ship, easy to run.


🐳 Dockerization

To ensure environment consistency, the application and its MySQL database were containerized using Docker. This eliminated “works on my machine” issues and laid the foundation for cloud deployment.

However, containers alone aren’t enough for production.


☸️ Kubernetes & AKS Deployment

The next step was Kubernetes orchestration, followed by migration to Azure Kubernetes Service (AKS) for managed scalability and reliability.

Key milestones during this phase:

Deploying services and deployments on AKS

Managing persistent storage using Kubernetes volumes

Exposing services securely via Ingress


🌐 Domain & Security

To make the application publicly accessible and secure:

DuckDNS was used to configure a free, dynamic custom domain

cert-manager was integrated to automate TLS certificate management

Let’s Encrypt provided HTTPS using the HTTP-01 challenge

This resulted in a fully HTTPS-secured application with automatic certificate renewal.


⚠️ Challenges Faced (And Solved)

This project wasn’t without obstacles—and each one became a learning opportunity:

503 Service Unavailable errors due to misconfigured Ingress and service ports

AKS Node CPU exhaustion, which required scaling node pools

Multi-Attach Volume errors when pods attempted to mount the same disk across nodes

Debugging Kubernetes resources using real production logs and events

Each issue strengthened my understanding of Kubernetes internals and cloud operations.


✅ Final Outcome

Today, the application is live and production-ready:

Cloud-native

Fully containerized

Scalable via AKS

Secured with HTTPS

Automated from deployment to certificate renewal

This project proves one thing:

With enough troubleshooting and the right tools, the cloud is just a playground for great code.


🛠 Tech Stack Summary
Layer	Technology Used
Backend / UI	Spring Boot (Java) + Embedded Frontend
Database	MySQL (StatefulSet)
Containerization	Docker + Azure Container Registry (ACR)
Orchestration	Azure Kubernetes Service (AKS)
DNS	DuckDNS
Security	Let’s Encrypt + cert-manager (HTTP-01)


🧭 Lessons Learned
1️⃣ Infrastructure Matters

Node sizing is critical—small clusters fill up faster than you expect.

2️⃣ Logs Are Your Best Friend

kubectl describe and kubectl logs are your only maps in the Kubernetes wilderness.

3️⃣ Automate Everything

Let’s Encrypt and cert-manager remove the pain from SSL—once you get past the dreaded “Pending” state
