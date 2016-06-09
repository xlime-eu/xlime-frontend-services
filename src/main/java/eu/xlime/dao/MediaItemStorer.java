package eu.xlime.dao;

import eu.xlime.bean.MediaItem;

public interface MediaItemStorer {

	/**
	 * Inserts (or replaces?) a mediaItem onto the back-end database.
	 *  
	 * @param mediaItem
	 * @return
	 */
	<T extends MediaItem> String insertOrUpdate(T mediaItem);
}
