package com.arashforus.nearroom

import android.content.Context
import android.database.Cursor
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.loader.content.CursorLoader

class MyGalleryTools(val context: Context) {

    fun getPictureAlbums(): ArrayList<Object_Album> {
        val albumsList = ArrayList<Object_Album>()
        val uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        val projection = arrayOf(
            MediaStore.Images.ImageColumns.BUCKET_ID,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATE_ADDED,
            MediaStore.Images.ImageColumns.DATA
        )
        val sortOrder = "${MediaStore.Images.ImageColumns.DATE_ADDED} DESC"
        //val groupBy = "1) GROUP BY 1,(2"

        val cursor = context.contentResolver.query(uri, projection, null, null, sortOrder)
        if (cursor!!.moveToFirst()) {
            do {
                val foldername =cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME))
                //println(foldername)
                val duplicate = albumsList.find {
                    it.folderName == foldername
                    }
                if ( duplicate == null ){
                    albumsList.add(
                        Object_Album(
                            "PICTURE",
                            cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)),
                            1,
                            cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED)),
                        )
                    )
                }else{
                    duplicate.imageCount = duplicate.imageCount + 1
                }
            }while (cursor.moveToNext())
        }
        cursor.close()
        return albumsList
    }

    fun getPicturesFromAlbum(bucketId: String): ArrayList<Object_Media> {
        val mediaList = ArrayList<Object_Media>()

        val uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        val projection = arrayOf(
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATE_ADDED,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.SIZE,
            MediaStore.Images.ImageColumns.MINI_THUMB_MAGIC,
            MediaStore.Images.ImageColumns.DATA
        )
        val sortOrder = "${MediaStore.Images.ImageColumns.DATE_ADDED} DESC"
        val selection = "${MediaStore.Images.ImageColumns.BUCKET_ID} = $bucketId"
        //println(bucketId)
        val cursor = context.contentResolver.query(uri, projection, selection, null, sortOrder)

        if (cursor!!.moveToFirst()) {
            do {
                mediaList.add(
                    Object_Media(
                        "PICTURE",
                        bucketId,
                        null,
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DISPLAY_NAME)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Images.ImageColumns.SIZE)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_ADDED)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.MIME_TYPE))
                    )
                )
            } while (cursor.moveToNext())

        }
        cursor.close()
        println("The size of get images is :")
        println(mediaList.size)
        return mediaList
    }

    fun getVideosAlbums(): ArrayList<Object_Album> {
        val albumsList = ArrayList<Object_Album>()
        val uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
        val projection = arrayOf(
            MediaStore.Video.VideoColumns.BUCKET_ID,
            MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DATE_ADDED,
            MediaStore.Video.VideoColumns.DATA
        )
        val sortOrder = "${MediaStore.Video.VideoColumns.DATE_ADDED} DESC"

        val cursor = context.contentResolver.query(uri, projection, null, null, sortOrder)
        if (cursor!!.moveToFirst()) {
            do {
                val foldername =cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME))
                //println(foldername)
                val duplicate = albumsList.find {
                    it.folderName == foldername
                }
                if ( duplicate == null ){
                    albumsList.add(
                        Object_Album(
                            "VIDEO",
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_ID)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)),
                            1,
                            cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_ADDED)),
                        )
                    )
                }else{
                    duplicate.imageCount = duplicate.imageCount + 1
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return albumsList
    }

    fun getVideosFromAlbum(bucketId: String): ArrayList<Object_Media> {
        val mediaList = ArrayList<Object_Media>()

        val uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
        val projection = arrayOf(
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DATE_ADDED,
            MediaStore.Video.VideoColumns.MIME_TYPE,
            MediaStore.Video.VideoColumns.SIZE,
            MediaStore.Video.VideoColumns.MINI_THUMB_MAGIC,
            MediaStore.Video.VideoColumns.DATA
        )
        val sortOrder = "${MediaStore.Video.VideoColumns.DATE_ADDED} DESC"
        val selection = "${MediaStore.Video.VideoColumns.BUCKET_ID} = $bucketId"
        //println(bucketId)
        val cursor = context.contentResolver.query(uri, projection, selection, null, sortOrder)

        if (cursor!!.moveToFirst()) {
            do {
                mediaList.add(
                    Object_Media(
                        "VIDEO",
                        bucketId,
                        null,
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns._ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DISPLAY_NAME)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATA)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Video.VideoColumns.SIZE)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.DATE_ADDED)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Video.VideoColumns.MIME_TYPE))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        //println(mediaList.size)
        return mediaList
    }

    fun getAudiosAlbums(): ArrayList<Object_Album> {
        val albumsList = ArrayList<Object_Album>()
        val uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        val projection = arrayOf(
            MediaStore.Audio.Media.BUCKET_ID,
            MediaStore.Audio.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID
        )
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        val cursor = context.contentResolver.query(uri, projection, null, null, sortOrder)
        if (cursor!!.moveToFirst()) {
            do {
                val foldername =cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME))
                val duplicate = albumsList.find {
                    it.folderName == foldername
                }
                if ( duplicate == null ){
                    albumsList.add(
                        Object_Album(
                            "AUDIO",
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.BUCKET_ID)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.BUCKET_DISPLAY_NAME)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                            1,
                            cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)),
                        )
                    )
                }else{
                    duplicate.imageCount = duplicate.imageCount + 1
                }
            } while (cursor.moveToNext())
        }
        cursor.close()
        return albumsList
    }

    fun getAudiosFromAlbum(bucketId: String): ArrayList<Object_Media> {
        val mediaList = ArrayList<Object_Media>()

        val uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.BUCKET_ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
        )
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        val selection = "${MediaStore.Audio.Media.BUCKET_ID} = $bucketId"
        val cursor = context.contentResolver.query(uri, projection, selection, null, sortOrder)
        if (cursor!!.moveToFirst()) {
            do {
                mediaList.add(
                    Object_Media(
                        "AUDIO",
                        null,
                        null,
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA)),
                        cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)),
                        cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        return mediaList
    }

    fun getListOfVideoFolders(albumsList: ArrayList<Object_Album>): ArrayList<Object_Album> {
        val cursor: Cursor
        var cursorBucket: Cursor
        val BUCKET_GROUP_BY = "1) GROUP BY 1,(2"
        val BUCKET_ORDER_BY = "MAX(datetaken) DESC"
        val column_index_album_name: Int
        val column_index_album_video: Int
        val uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

        val projection1 = arrayOf(
            MediaStore.Video.VideoColumns.BUCKET_ID,
            MediaStore.Video.VideoColumns.BUCKET_DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DATE_TAKEN,
            MediaStore.Video.VideoColumns.DATA
        )

        cursor = context.contentResolver.query(
            uri,
            projection1,
            BUCKET_GROUP_BY,
            null,
            BUCKET_ORDER_BY
        )!!

        if (cursor != null) {
            column_index_album_name = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
            column_index_album_video = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            while (cursor.moveToNext()) {
                Log.d("title_apps", "bucket video:" + cursor.getString(column_index_album_name))
                Log.d("title_apps", "bucket video:" + cursor.getString(column_index_album_video))
                val selectionArgs = arrayOf("%" + cursor.getString(column_index_album_name) + "%")

                val selection = MediaStore.Video.Media.DATA + " like ? "
                val projectionOnlyBucket = arrayOf(
                    MediaStore.MediaColumns.DATA,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME
                )

                cursorBucket = context.contentResolver.query(
                    uri,
                    projectionOnlyBucket,
                    selection,
                    selectionArgs,
                    null
                )!!
                Log.d("title_apps", "bucket size:" + cursorBucket.count)

                //albumsList.add(Object_Album(cursor.getString(column_index_album_name), cursor.getString(column_index_album_video), cursorBucket.count, true))
            }
        }
        return albumsList
    }

    fun getImagesAndVideosFromFiles(){
        val projection = arrayOf(
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE
        )

        val selection = (MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "="
                + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)

        val queryUri = MediaStore.Files.getContentUri("external")

        val cursorLoader = CursorLoader(
            context,
            queryUri,
            projection,
            selection,
            null,
            MediaStore.Files.FileColumns.DATE_ADDED + " DESC"
        )

        //val cursor: Cursor = cursorLoader.loadInBackground()
    }

    /*fun getImages(): ArrayList<GalleryImage> {
        val images: ArrayList<GalleryImage> = ArrayList()
        try {
            if (isReadWritePermitted()) {
                val imagesProjection = arrayOf(MediaStore.Images.Media._ID,
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media.DATE_MODIFIED,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.HEIGHT,
                    MediaStore.Images.Media.MIME_TYPE,
                    MediaStore.Images.Media.SIZE,
                    MediaStore.Images.Media.TITLE,
                    MediaStore.Images.Media.WIDTH,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.BUCKET_ID,
                    MediaStore.Images.Media.DATE_TAKEN,
                    MediaStore.Images.Media.DESCRIPTION,
                    MediaStore.Images.Media.IS_PRIVATE,
                    MediaStore.Images.Media.LATITUDE,
                    MediaStore.Images.Media.LONGITUDE,
                    MediaStore.Images.Media.MINI_THUMB_MAGIC,
                    MediaStore.Images.Media.ORIENTATION,
                    MediaStore.Images.Media.PICASA_ID)
                val imagesQueryUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                val imagescursor = ctx.contentResolver.query(imagesQueryUri, imagesProjection, null, null, null)

                if (imagescursor != null && imagescursor.count > 0) {
                    if (imagescursor.moveToFirst()) {
                        do {
                            val image = GalleryImage()
                            image.ID = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media._ID))
                            image.DATA = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.DATA))
                            image.DATE_ADDED = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED))
                            image.DATE_MODIFIED = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED))
                            image.DISPLAY_NAME = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
                            image.HEIGHT = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.HEIGHT))
                            image.MIME_TYPE = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.MIME_TYPE))
                            image.SIZE = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.SIZE))
                            image.TITLE = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.TITLE))
                            image.WIDTH = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.WIDTH))
                            image.BUCKET_DISPLAY_NAME = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME))
                            image.BUCKET_ID = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID))
                            image.DATE_TAKEN = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN))
                            image.DESCRIPTION = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.DESCRIPTION))
                            image.IS_PRIVATE = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.IS_PRIVATE))
                            image.LATITUDE = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.LATITUDE))
                            image.LONGITUDE = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.LONGITUDE))
                            image.MINI_THUMB_MAGIC = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.MINI_THUMB_MAGIC))
                            image.ORIENTATION = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.ORIENTATION))
                            image.PICASA_ID = imagescursor.getString(imagescursor.getColumnIndex(MediaStore.Images.Media.PICASA_ID))
                            image.ALBUM_NAME = File(image.DATA).parentFile.name
                            images.add(image)
                        } while (imagescursor.moveToNext())
                    }
                    imagescursor.close()
                }
            }
        } catch (e: Exception) {
            Log.e("IMAGES", e.toString())
        } finally {
            return images
        }
    }

    fun getVideos(): ArrayList<GalleryVideo> {
        val videos: ArrayList<GalleryVideo> = ArrayList()
        try {
            if (isReadWritePermitted()) {
                val videoProjection = arrayOf(MediaStore.Images.Media._ID,
                    MediaStore.Video.Media.DATA,
                    MediaStore.Video.Media.DATE_ADDED,
                    MediaStore.Video.Media.DATE_MODIFIED,
                    MediaStore.Video.Media.DISPLAY_NAME,
                    MediaStore.Video.Media.HEIGHT,
                    MediaStore.Video.Media.MIME_TYPE,
                    MediaStore.Video.Media.SIZE,
                    MediaStore.Video.Media.TITLE,
                    MediaStore.Video.Media.WIDTH,
                    MediaStore.Video.Media.ALBUM,
                    MediaStore.Video.Media.ARTIST,
                    MediaStore.Video.Media.BOOKMARK,
                    MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Video.Media.BUCKET_ID,
                    MediaStore.Video.Media.CATEGORY,
                    MediaStore.Video.Media.DATE_TAKEN,
                    MediaStore.Video.Media.DESCRIPTION,
                    MediaStore.Video.Media.DURATION,
                    MediaStore.Video.Media.IS_PRIVATE,
                    MediaStore.Video.Media.LANGUAGE,
                    MediaStore.Video.Media.LATITUDE,
                    MediaStore.Video.Media.LONGITUDE,
                    MediaStore.Video.Media.MINI_THUMB_MAGIC,
                    MediaStore.Video.Media.RESOLUTION,
                    MediaStore.Video.Media.TAGS)

                val videoQueryUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI

                val videocursor = ctx.contentResolver.query(videoQueryUri, videoProjection, null, null, null)

                if (videocursor != null && videocursor.count > 0) {
                    if (videocursor.moveToFirst()) {
                        do {
                            val video = GalleryVideo()
                            video.ID = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media._ID))
                            video.DATA = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.DATA))
                            video.DATE_ADDED = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED))
                            video.DATE_MODIFIED = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED))
                            video.DISPLAY_NAME = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME))
                            video.HEIGHT = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.HEIGHT))
                            video.MIME_TYPE = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE))
                            video.SIZE = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                            video.TITLE = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                            video.WIDTH = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.WIDTH))
                            video.ALBUM = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.ALBUM))
                            video.ARTIST = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.ARTIST))
                            video.BOOKMARK = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.BOOKMARK))
                            video.BUCKET_DISPLAY_NAME = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                            video.BUCKET_ID = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
                            video.CATEGORY = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.CATEGORY))
                            video.DATE_TAKEN = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.DATE_TAKEN))
                            video.DESCRIPTION = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.DESCRIPTION))
                            video.DURATION = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.DURATION))
                            video.IS_PRIVATE = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.IS_PRIVATE))
                            video.LANGUAGE = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.LANGUAGE))
                            video.LATITUDE = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.LATITUDE))
                            video.LONGITUDE = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.LONGITUDE))
                            video.MINI_THUMB_MAGIC = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.MINI_THUMB_MAGIC))
                            video.RESOLUTION = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.RESOLUTION))
                            video.TAGS = videocursor.getString(videocursor.getColumnIndex(MediaStore.Video.Media.TAGS))
                            video.ALBUM_NAME = File(video.DATA).parentFile.name
                            videos.add(video)
                        } while (videocursor.moveToNext())
                    }
                    videocursor.close()
                }
            }
        } catch (e: Exception) {
            Log.e("VIDEOS", e.toString())
        } finally {
            return videos
        }
    }*/


}