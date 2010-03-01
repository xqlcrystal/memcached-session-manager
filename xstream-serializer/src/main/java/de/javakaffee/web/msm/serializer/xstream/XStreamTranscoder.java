/*
 * Copyright 2009 Martin Grotzke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package de.javakaffee.web.msm.serializer.xstream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

import org.apache.catalina.session.StandardSession;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import com.thoughtworks.xstream.XStream;

import de.javakaffee.web.msm.SessionAttributesTranscoder;
import de.javakaffee.web.msm.MemcachedBackupSessionManager.MemcachedBackupSession;

/**
 * A {@link net.spy.memcached.transcoders.Transcoder} that serializes catalina
 * {@link StandardSession}s using <a href="http://xstream.codehaus.org/">XStream</a> (xml).
 * 
 * @author <a href="mailto:martin.grotzke@javakaffee.de">Martin Grotzke</a>
 */
public class XStreamTranscoder implements SessionAttributesTranscoder {

    private static final Log LOG = LogFactory.getLog( XStreamTranscoder.class );
    
    private final XStream _xstream;

    /**
     * Constructor.
     * 
     * @param manager
     *            the manager
     */
    public XStreamTranscoder() {
        _xstream = new XStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] serialize( final MemcachedBackupSession session, final Map<String, Object> attributes ) {
        if ( attributes == null ) {
            throw new NullPointerException( "Can't serialize null" );
        }

        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            _xstream.toXML( attributes, bos );
            return bos.toByteArray();
        } catch ( final Exception e ) {
            throw new IllegalArgumentException( "Non-serializable object", e );
        } finally {
            closeSilently( bos );
        }

    }

    /**
     * Get the object represented by the given serialized bytes.
     * 
     * @param in
     *            the bytes to deserialize
     * @return the resulting object
     */
    @Override
    public Map<String, Object> deserialize( final byte[] in ) {
        final ByteArrayInputStream bis = new ByteArrayInputStream( in );
        try {
            @SuppressWarnings( "unchecked" )
            final Map<String, Object> result = (Map<String, Object>) _xstream.fromXML( bis );
            return result;
        } catch ( final RuntimeException e ) {
            LOG.warn( "Caught Exception decoding "+ in.length +" bytes of data", e );
            throw e ;
        } finally {
            closeSilently( bis );
        }
    }

    private void closeSilently( final Closeable stream ) {
        if ( stream != null ) {
            try {
                stream.close();
            } catch ( final IOException f ) {
                // fail silently
            }
        }
    }

}