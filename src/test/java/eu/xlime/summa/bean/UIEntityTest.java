package eu.xlime.summa.bean;

import java.io.ObjectOutputStream;

import org.junit.Test;

import eu.xlime.summa.bean.UIEntity;

public class UIEntityTest {

	@Test
	public void testSerialization() throws Exception {
		UIEntity entity = new UIEntity();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(System.out);
        objectOutputStream.writeObject(entity);
        objectOutputStream.flush();
	}
}
