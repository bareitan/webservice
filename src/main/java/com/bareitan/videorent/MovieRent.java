package com.bareitan.videorent;

/**
 * Created by bareitan on 31/03/2017.
 */
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;


@ApplicationPath("/")
public class MovieRent extends Application{
    @Override
    public Set<Class<?>> getClasses() {
        HashSet h = new HashSet<Class<?>>();
        h.add( Register.class );
        h.add( Login.class );
        h.add(Movies.class);
        return h;
    }
}