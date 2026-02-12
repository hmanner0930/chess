# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```


## Sequence Diagram Link
https://sequencediagram.org/index.html?presentationMode=readOnly&shrinkToFit=true#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAoMlkcpQMgBHVI5ACU12qojulVk8iUKnU9XsKDAAFUBhi3h8UKTqYplGpVJSjDpagAxJCcGCsyg8mA6SwwDmzMQ6FHAADWkoGME2SDA8QVA05MGACFVHHlKAAHmiNDzafy7gjySp6lKoDyySIVI7KjdnjAFKaUMBze11egAKKWlTYAgFT23Ur3YrmeqBJzBYbjObqYCMhbLCNQbx1A1TJXGoMh+XyNXoKFmTiYO189Q+qpelD1NA+BAIBMU+4tumqWogVXot3sgY87nae1t+7GWoKDgcTXS7QD71D+et0fj4PohQ+PUY4Cn+Kz5t7keC5er9cnvUexE7+4wp6l7FovFqXtYJ+cLtn6pavIaSpLPU+wgheertBAdZoFByyXAmlDtimGD1OEThOFmEwQZ8MDQcCyxwfECFISh+xXOgHCmF4vgBNA7CMjEIpwBG0hwAoMAADIQFkhRYcwTrUP6zRtF0vQGOo+RoFmipzGsvz-BwVygYKQH+iMyzKfMizfGpOx7Es0KPMB4lUEiMAIEJ4oYvZwkEkSYCkm+hi7jS+4MkyU4GXOPl3kuwowGKEpulo8jarq+oGTewUOkmvrOl2G7utoMDQDAPZ9pgKrBhqACSaBUCaSDrgZPQmQC25edZ-rMtMl7QEgABeKAMVpKXwMgqaNOmACM3RZssOaqHmhmFsW0D1D4LV6m1nW7DAdFNsOyWVMuAZrgGl4epti7WbZz5Xlunntjp37-gggGWRhvWgbUemEeWnxGbBl5UfWRlreh8LJv12GNLhTidGNb3vJBn3kd9iG-WM5mNgxnjeH4-heCg6AxHEiRYzjgn2L4WCiYKz2NNIEb8RG7QRt0PRyaoCnDBRP2FD1lTXfUbMI4U13k2l9TOSTTlCSTrlqO59WCkdo4wIyYBnee8NIUFvIhdtYUAGbik+B1ZTqeowJASGJRryUnS6+0vllOV5f2hXqjApXlcgHA9DM2TMw4xqq+gMtPdUTWLfEy1dQDmHA2A9QNMNEOjON-JTQWYxFiW82h+Hq3rd1aWy7e-L1BwKDcOi7Mq-BfPqwuAqhciRwQDQAoUYdhfHalnb1GQPj7oHH4PaWRMk5gAtBxJYGacHgOVKJOF4VmueMejAQouu-jYOKGr8WiMAAOJKhoZONaWDR77TDP2EqrP+xz0-aYPPO36Pg+C13dlogfOZOWikskoHVJ27y0VsrXmatza13vGFCK65W6GziibPmED9wgSFjbc6MV7a9kdqqEqZUKoey9vJX2YCA6XXHjUV0WcoAdQjj1IGJQY6DScCNGAkMJop2gunOaCpqG0JzijABMggHC0-ofDENcUH133kySsCB96H2QSFK26Ud45AxF-NQHl869W5gJX+t0wAYivjmUkY8T4vGWCYtQBYGjjGscVaQBYhrhGCIEEEmx4i6hQG6TkZkQTJFAGqXxMMkYgmsQAOSVEZC4MBOhTwnlHRhscwajUTmMaxqhbH2KVI4sYnjvEhI+mE5YgSQDBKIoZEpGSlRRLmDEuJS80bMX8BwAA7G4JwKAnAxAjMEOAXEABs8AJyGE0TAIo0c36UMaK0Dol9r6h3ZlmSJSoEmJi5o-P2VckJrByXMOpKAADk9QjkNjHp3Gy1tDxyBQJojENz0R-2luQwBSV6QKyZKA2+kjNZCnqDA9BMojb6lNgHOWqD35nRlFg-KTs8Fu0qp7b24pHDs37pckOFFs6R16nPZhI12HJ3zFw2apYFrYpoStZG9EhFy3qI8u5SpjG1KVL8ra-yXZoAGGAAUaAUCbAUXMNu7y64qPqJo4FCDrGVQxQ8WE-pGWaIyKoACLK5iVTMa-ChukxiVQWC4tx6zHoMIGqkyGDinH1ENYEf6tLmkY0sKXeygrYhIASGAJ1fYICCoAFIQHFEKww-gylqkmYw6ZkkmjMhkj0axN8dnoCzNgBAwAnVQDgBAeyUA1iWuNfCTZCrSykLQHs9Yqb02ZuzXsAA6iwYqdMegACF+IKDgAAaW+Ja5xrjbUWSLdM2yAArANaB7kjvFM87RnYC6ir8krS8ldKLVyURynagKzqxWNuiiFFDToG0wbACJ2CCq4K5Yij2ABZFFvt0WeRShTZqlL+G4tNSDOOLC0mvQ4SSmaGdeHPupXaps97yj0s+WAe5lr2XqAqOuvWQapXG1lbu8ViG7awAdqeoqMgIBqDQEc5gUY0T1QfdPeoTaQwwGea+2e0dagfvBmw9JP7pppzJfUPQ64USEiljSkDOi3kWw+XAeIKBykwBAFm6AdKREKmwNFJlcwMSVQ4CsTkawpPZpg3XLWroFO3PQ-IVdHcOxXPSj3Pu5CB5FvqCqgCL8B06rqCMfNSSBoNHNaMJpTEMZeDTbjd1+MoABcQMGWAwBsApsIHkAoEzj6YtPlTGmdMGbGFxYWr8DLuB4GuhIxzX5B3XJy1Aezd1JMlfy684Rc6KvhbdKofLu69NGFLkyNQciyywGisAEzYrLm2VVVZwTNXhPyxACV5WOmoHFza+iFufZtn6h631yF5n6hDZHLJ2rE3wuaMa9N6RJcy4dZNPI6xRgtyoYG9bTb-I5V6N23gGAKJVV3QK1ZCxMARhoAgHqVFbm8X0ZgGDRemAeg9GKkYSqMBVAQDmC9tQ4WQD6ji2HQVuptS+AQOuY77XOtLQMCbLxDgBRQ-+2gDUmPsgwGwBAeSyRDCQCMBAGAmOzvGmYAUb0OhTzE8MNgEL4AKQ0+QHWYnNPtbeBmNqeIcgXYwDVL9wVxxLA6EMJYXwuVsawNh9gcTSBdbRDlwO5neP0RyJcIIoAA
