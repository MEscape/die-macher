### Raspi

cd app/raspi
scripts/test

docker run --rm `
  --network ci_sonarnet `
  -e SONAR_HOST_URL="http://die-macher-sonarqube:9000" `
  -e SONAR_TOKEN="squ_3688f91a4c95e6742346b5ad01e3ec5d56f983d3" `
  -v "C:/Users/ME90678/Desktop/Programming/Die-Macher/app/raspi:/usr/src" `
  sonarsource/sonar-scanner-cli

---
### System 1

cd app/system_1
mvn clean verify

docker run --rm `
  --network ci_sonarnet `
  -e SONAR_HOST_URL="http://die-macher-sonarqube:9000" `
  -e SONAR_TOKEN="squ_3688f91a4c95e6742346b5ad01e3ec5d56f983d3" `
  -v "C:/Users/ME90678/Desktop/Programming/Die-Macher/app/system_1:/usr/src" `
  sonarsource/sonar-scanner-cli

---
### System 2

cd app/system_2
mvn clean verify

docker run --rm `
  --network ci_sonarnet `
  -e SONAR_HOST_URL="http://die-macher-sonarqube:9000" `
  -e SONAR_TOKEN="squ_3688f91a4c95e6742346b5ad01e3ec5d56f983d3" `
  -v "C:/Users/ME90678/Desktop/Programming/Die-Macher/app/system_2:/usr/src" `
  sonarsource/sonar-scanner-cli
