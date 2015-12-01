package fr.commandestation.station;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@SuppressWarnings("serial")
public class CSerializable implements Serializable {

    public byte[] serialize () throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream ();
        ObjectOutput out = new ObjectOutputStream (bos);
        out.writeObject (this);
        out.close ();
        return bos.toByteArray ();
    } 

    public Object unserialize (byte[] b) throws IOException,
            ClassNotFoundException {
        ByteArrayInputStream bis = new ByteArrayInputStream (b);
        ObjectInputStream in = new ObjectInputStream (bis);
        Object object = in.readObject ();
        bis.close ();
        in.close ();
        return object;
    } 

}; // CSerializable;