package com.smartcampus.resource;

import com.smartcampus.dao.DataStore;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.exception.DataNotFoundException;
import com.smartcampus.model.Room;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;

@Path("/rooms")
public class RoomResource {

    // GET /api/v1/rooms — returns all rooms
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        return Response.ok(new ArrayList<>(DataStore.rooms.values())).build();
    }

    // POST /api/v1/rooms — creates a new room
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(400)
                    .entity("{\"error\": \"Room ID is required\"}")
                    .build();
        }
        if (DataStore.rooms.containsKey(room.getId())) {
            return Response.status(409)
                    .entity("{\"error\": \"Room with this ID already exists\"}")
                    .build();
        }
        DataStore.rooms.put(room.getId(), room);
        return Response.status(201).entity(room).build();
    }

    // GET /api/v1/rooms/{roomId} — returns one specific room
    @GET
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            throw new DataNotFoundException("Room not found with ID: " + roomId);
        }
        return Response.ok(room).build();
    }

    // DELETE /api/v1/rooms/{roomId} — deletes a room if it has no sensors
    @DELETE
    @Path("/{roomId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.rooms.get(roomId);
        if (room == null) {
            throw new DataNotFoundException("Room not found with ID: " + roomId);
        }
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Cannot delete room " + roomId + 
                " — it still has active sensors assigned to it."
            );
        }
        DataStore.rooms.remove(roomId);
        return Response.noContent().build();
    }
}