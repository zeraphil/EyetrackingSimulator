
# Eyetracking Simulator

Eyetracking simulator built for Android Oreo and test on Android Emulator and Google Pixel.

To run, open the project, let Gradle sync, and build the APK. 
All dependencies are built into Gradle.

## Structure

The project is composed of two logical components: an eyetracking data consumer, and an eyetracking data producer. In reality, the structure is broken down further to appease modularity. 

- Simulator: Simulators implement an update method that generates a packet of eyetracking data in the schema provided.
- Service: Service implementations for both production and consumption of eyetracking dataa
- DataModel: data classes and database descriptions, including the schema for the database and the schema for the flatbuffer transport library
- Tasks : AsyncTasks, not currently used
- UI: custom View and class for drawing the "eye positions"
- Tests: Instrumentation tests, mainly for the serialization processes

 Timestamp timestamp { long seconds, int nanoseconds};
 boolean id;
 float confidence;
 float normalizedPosX;
 float normalizedPosY;
 int pupilDiameter;

 an additional data memeber, uniqueID, was added for testing and database insertion as primary key.


#### Simulators
 Two simulators are provided: random, and eyeball, with random being used for testing, and eyeball being used for the app itself.
 Eyeball tries to simulate eye movement a bit closer than just pure random movement. It does this by approximating the chord length described by an saccade at each time step, assuming a fixed distance from "eyes" to "screen", using a target to simulate "where the eyes would be looking at"

#### Service
 Services are further broken down into two main parts: The messenger services and the database service. The Messenger services both inherit form BaseEyetrackingService, which provides the simulation logic and the incoming message handling logic. After this, two services were created: EyetrackingMessengerService, which does IPC to the activity through the Binder framework and parcel transport library, and EyetrackingFlatBufferService, which also uses the Binder framework but uses Flatbuffers as the serializer. The second service was created as an answer to the question of "what to use if using a wireless networking connection" rather than same device IPC. Flatbuffer serialization is so quick that the difference between it and Parcel is nearly neglibile.

 The other service, DatabaseRoomService, is an appropriately named service implementing Android's Room Sqlite wrapper for data persistence. This is done on a separate thread, with data dumps happening about once every second through the use of a LinkedBlockingQueue.

#### DataModel
Data Model implements the EyetrackingData model, which itself has methods for parcel and flat buffer serialization, with the latter meant for more general purpose serialization and the first meant for absolute speed. The database implementation uses Room, which simplifies the creation of an sqlite db in Android. so, a wrapper for creating a database entitiy object is in the EyetrackingData model as well, to Serializable. 

#### UI
A custom DrawView is used to represent the two eye positions, which transform the normalized positions into the view coordinates. Nothing special beyond that here.
