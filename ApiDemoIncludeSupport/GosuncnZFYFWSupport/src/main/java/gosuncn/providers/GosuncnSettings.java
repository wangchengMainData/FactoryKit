package gosuncn.providers;

import com.android.internal.util.ArrayUtils;

import android.content.ContentResolver;
import android.content.IContentProvider;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
//import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AndroidException;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;



/**
 * GosuncnSettings contains Gosuncn specific preferences in System, Secure, and Global.
 */
public final class GosuncnSettings {
    private static final String TAG = "GosuncnSettings";
    private static final boolean LOCAL_LOGV = true;

    public static final String AUTHORITY = "GosuncnSettings";

    public static class GosuncnSettingNotFoundException extends AndroidException {
        public GosuncnSettingNotFoundException(String msg) {
            super(msg);
        }
    }

    // region Call Methods

    /**
     * @hide - User handle argument extra to the fast-path call()-based requests
     */
    public static final String CALL_METHOD_USER_KEY = "_user";

    /**
     * @hide - Private call() method on SettingsProvider to read from 'system' table.
     */
    public static final String CALL_METHOD_GET_SYSTEM = "GET_system";

    /**
     * @hide - Private call() method on SettingsProvider to read from 'secure' table.
     */
    public static final String CALL_METHOD_GET_SECURE = "GET_secure";

    /**
     * @hide - Private call() method on SettingsProvider to read from 'global' table.
     */
    public static final String CALL_METHOD_GET_GLOBAL = "GET_global";

    /**
     * @hide - Private call() method to write to 'system' table
     */
    public static final String CALL_METHOD_PUT_SYSTEM = "PUT_system";

    /**
     * @hide - Private call() method to write to 'secure' table
     */
    public static final String CALL_METHOD_PUT_SECURE = "PUT_secure";

    /**
     * @hide - Private call() method to write to 'global' table
     */
    public static final String CALL_METHOD_PUT_GLOBAL= "PUT_global";

    /**
     * @hide - Private call() method on GosuncnSettingsProvider to migrate Gosuncn settings
     */
    public static final String CALL_METHOD_MIGRATE_SETTINGS = "migrate_settings";

    /**
     * @hide - Private call() method on GosuncnSettingsProvider to migrate Gosuncn settings for a user
     */
    public static final String CALL_METHOD_MIGRATE_SETTINGS_FOR_USER = "migrate_settings_for_user";

    // endregion

    // Thread-safe.
    private static class NameValueCache {
        private final String mVersionSystemProperty;
        private final Uri mUri;

        private static final String[] SELECT_VALUE_PROJECTION =
                new String[] { Settings.NameValueTable.VALUE };
        private static final String NAME_EQ_PLACEHOLDER = "name=?";

        // Must synchronize on 'this' to access mValues and mValuesVersion.
        private final HashMap<String, String> mValues = new HashMap<String, String>();
        private long mValuesVersion = 0;

        // Initially null; set lazily and held forever.  Synchronized on 'this'.
        private IContentProvider mContentProvider = null;

        // The method we'll call (or null, to not use) on the provider
        // for the fast path of retrieving settings.
        private final String mCallGetCommand;
        private final String mCallSetCommand;

        public NameValueCache(String versionSystemProperty, Uri uri,
                              String getCommand, String setCommand) {
            mVersionSystemProperty = versionSystemProperty;
            mUri = uri;
            mCallGetCommand = getCommand;
            mCallSetCommand = setCommand;
        }

        private IContentProvider lazyGetProvider(ContentResolver cr) {
            IContentProvider cp;
            synchronized (this) {
                cp = mContentProvider;
                if (cp == null) {
                    cp = mContentProvider = cr.acquireProvider(mUri.getAuthority());
                }
            }
            return cp;
        }

        /**
         * Puts a string name/value pair into the content provider for the specified user.
         * @param cr The content resolver to use.
         * @param name The name of the key to put into the content provider.
         * @param value The value to put into the content provider.
         * @param userId The user id to use for the content provider.
         * @return Whether the put was successful.
         */
        public boolean putStringForUser(ContentResolver cr, String name, String value,
                                        final int userId) {
            mValues.remove(name); // wmd
            try {
                Bundle arg = new Bundle();
                arg.putString(Settings.NameValueTable.VALUE, value);
                arg.putInt(CALL_METHOD_USER_KEY, userId);
                IContentProvider cp = lazyGetProvider(cr);
                cp.call(cr.getPackageName(), mCallSetCommand, name, arg);
            } catch (RemoteException e) {
                Log.w(TAG, "Can't set key " + name + " in " + mUri, e);
                return false;
            }
            return true;
        }

        /**
         * Gets a string value with the specified name from the name/value cache if possible. If
         * not, it will use the content resolver and perform a query.
         * @param cr Content resolver to use if name/value cache does not contain the name or if
         *           the cache version is older than the current version.
         * @param name The name of the key to search for.
         * @param userId The user id of the cache to look in.
         * @return The string value of the specified key.
         */
        public String getStringForUser(ContentResolver cr, String name, final int userId) {
            final boolean isSelf = (userId == UserHandle.myUserId());
            if (isSelf) {
                if (LOCAL_LOGV) Log.d(TAG, "get setting for self");
//                long newValuesVersion = SystemProperties.getLong(mVersionSystemProperty, 0);

                // Our own user's settings data uses a client-side cache
                synchronized (this) {
//                    if (mValuesVersion != newValuesVersion) {
//                        if (LOCAL_LOGV || false) {
//                            Log.v(TAG, "invalidate [" + mUri.getLastPathSegment() + "]: current "
//                                    + newValuesVersion + " != cached " + mValuesVersion);
//                        }
//
//                        mValues.clear();
//                        mValuesVersion = newValuesVersion;
//                    }

                    if (mValues.containsKey(name)) {
                        return mValues.get(name);  // Could be null, that's OK -- negative caching
                    }
                }
            } else {
                if (LOCAL_LOGV) Log.v(TAG, "get setting for user " + userId
                        + " by user " + UserHandle.myUserId() + " so skipping cache");
            }

            IContentProvider cp = lazyGetProvider(cr);

            // Try the fast path first, not using query().  If this
            // fails (alternate Settings provider that doesn't support
            // this interface?) then we fall back to the query/table
            // interface.
            if (mCallGetCommand != null) {
                try {
                    Bundle args = null;
                    if (!isSelf) {
                        args = new Bundle();
                        args.putInt(CALL_METHOD_USER_KEY, userId);
                    }
                    Bundle b = cp.call(cr.getPackageName(), mCallGetCommand, name, args);
                    if (b != null) {
//                        String value = b.getPairValue();
                        String value = b.getString(Settings.NameValueTable.VALUE);
                        // Don't update our cache for reads of other users' data
                        if (isSelf) {
                            synchronized (this) {
                                mValues.put(name, value);
                            }
                        } else {
                            if (LOCAL_LOGV) Log.i(TAG, "call-query of user " + userId
                                    + " by " + UserHandle.myUserId()
                                    + " so not updating cache");
                        }
                        return value;
                    }
                    // If the response Bundle is null, we fall through
                    // to the query interface below.
                } catch (RemoteException e) {
                    // Not supported by the remote side?  Fall through
                    // to query().
                }
            }

            Cursor c = null;
            try {
                Bundle queryArgs = ContentResolver.createSqlQueryBundle(
                        NAME_EQ_PLACEHOLDER, new String[]{name}, null);
                c = cp.query(cr.getPackageName(), mUri, SELECT_VALUE_PROJECTION, queryArgs, null);
                if (c == null) {
                    Log.w(TAG, "Can't get key " + name + " from " + mUri);
                    return null;
                }

                String value = c.moveToNext() ? c.getString(0) : null;
                synchronized (this) {
                    mValues.put(name, value);
                }
                if (LOCAL_LOGV) {
                    Log.v(TAG, "cache miss [" + mUri.getLastPathSegment() + "]: " +
                            name + " = " + (value == null ? "(null)" : value));
                }
                return value;
            } catch (RemoteException e) {
                Log.w(TAG, "Can't get key " + name + " from " + mUri, e);
                return null;  // Return null, but don't cache it.
            } finally {
                if (c != null) c.close();
            }
        }
    }

    // region Validators

    /** @hide */
    public static interface Validator {
        public boolean validate(String value);
    }

    private static final Validator sBooleanValidator =
            new DiscreteValueValidator(new String[] {"0", "1"});

    private static final Validator sNonNegativeIntegerValidator = new Validator() {
        @Override
        public boolean validate(String value) {
            try {
                return Integer.parseInt(value) >= 0;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    };

    private static final Validator sUriValidator = new Validator() {
        @Override
        public boolean validate(String value) {
            try {
                Uri.decode(value);
                return true;
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
    };

    private static final Validator sColorValidator =
            new InclusiveIntegerRangeValidator(Integer.MIN_VALUE, Integer.MAX_VALUE);

    private static final Validator sAlwaysTrueValidator = new Validator() {
        @Override
        public boolean validate(String value) {
            return true;
        }
    };

    private static final Validator sNonNullStringValidator = new Validator() {
        @Override
        public boolean validate(String value) {
            return value != null;
        }
    };

    private static final class DiscreteValueValidator implements Validator {
        private final String[] mValues;

        public DiscreteValueValidator(String[] values) {
            mValues = values;
        }

        @Override
        public boolean validate(String value) {
            return ArrayUtils.contains(mValues, value);
        }
    }

    private static final class InclusiveIntegerRangeValidator implements Validator {
        private final int mMin;
        private final int mMax;

        public InclusiveIntegerRangeValidator(int min, int max) {
            mMin = min;
            mMax = max;
        }

        @Override
        public boolean validate(String value) {
            try {
                final int intValue = Integer.parseInt(value);
                return intValue >= mMin && intValue <= mMax;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    private static final class InclusiveFloatRangeValidator implements Validator {
        private final float mMin;
        private final float mMax;

        public InclusiveFloatRangeValidator(float min, float max) {
            mMin = min;
            mMax = max;
        }

        @Override
        public boolean validate(String value) {
            try {
                final float floatValue = Float.parseFloat(value);
                return floatValue >= mMin && floatValue <= mMax;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    private static final class DelimitedListValidator implements Validator {
        private final ArraySet<String> mValidValueSet;
        private final String mDelimiter;
        private final boolean mAllowEmptyList;

        public DelimitedListValidator(String[] validValues, String delimiter,
                                      boolean allowEmptyList) {
            mValidValueSet = new ArraySet<String>(Arrays.asList(validValues));
            mDelimiter = delimiter;
            mAllowEmptyList = allowEmptyList;
        }

        @Override
        public boolean validate(String value) {
            ArraySet<String> values = new ArraySet<String>();
            if (!TextUtils.isEmpty(value)) {
                final String[] array = TextUtils.split(value, Pattern.quote(mDelimiter));
                for (String item : array) {
                    if (TextUtils.isEmpty(item)) {
                        continue;
                    }
                    values.add(item);
                }
            }
            if (values.size() > 0) {
                values.removeAll(mValidValueSet);
                // values.size() will be non-zero if it contains any values not in
                // mValidValueSet
                return values.size() == 0;
            } else if (mAllowEmptyList) {
                return true;
            }

            return false;
        }
    }
    // endregion Validators

    /**
     * System settings, containing miscellaneous Gosuncn system preferences. This table holds simple
     * name/value pairs. There are convenience functions for accessing individual settings entries.
     */
    public static final class System extends Settings.NameValueTable {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/system");

        public static final String SYS_PROP_GOSUNCN_SETTING_VERSION = "sys.gosuncn_settings_system_version";

        private static final NameValueCache sNameValueCache = new NameValueCache(
                SYS_PROP_GOSUNCN_SETTING_VERSION,
                CONTENT_URI,
                CALL_METHOD_GET_SYSTEM,
                CALL_METHOD_PUT_SYSTEM);

        /** @hide */
        protected static final ArraySet<String> MOVED_TO_SECURE;
        static {
            MOVED_TO_SECURE = new ArraySet<>(1);
        }

        // region Methods

        /**
         * Put a delimited list as a string
         * @param resolver to access the database with
         * @param name to store
         * @param delimiter to split
         * @param list to join and store
         * @hide
         */
        public static void putListAsDelimitedString(ContentResolver resolver, String name,
                                                    String delimiter, List<String> list) {
            String store = TextUtils.join(delimiter, list);
            putString(resolver, name, store);
        }

        /**
         * Get a delimited string returned as a list
         * @param resolver to access the database with
         * @param name to store
         * @param delimiter to split the list with
         * @return list of strings for a specific Settings.Secure item
         * @hide
         */
        public static List<String> getDelimitedStringAsList(ContentResolver resolver, String name,
                                                            String delimiter) {
            String baseString = getString(resolver, name);
            List<String> list = new ArrayList<String>();
            if (!TextUtils.isEmpty(baseString)) {
                final String[] array = TextUtils.split(baseString, Pattern.quote(delimiter));
                for (String item : array) {
                    if (TextUtils.isEmpty(item)) {
                        continue;
                    }
                    list.add(item);
                }
            }
            return list;
        }

        /**
         * Construct the content URI for a particular name/value pair, useful for monitoring changes
         * with a ContentObserver.
         * @param name to look up in the table
         * @return the corresponding content URI
         */
        public static Uri getUriFor(String name) {
            return Settings.NameValueTable.getUriFor(CONTENT_URI, name);
        }

        /**
         * Look up a name in the database.
         * @param resolver to access the database with
         * @param name to look up in the table
         * @return the corresponding value, or null if not present
         */
        public static String getString(ContentResolver resolver, String name) {
            return getStringForUser(resolver, name, UserHandle.myUserId());
        }

        /**
         * Look up a name in the database.
         * @param resolver to access the database with
         * @param name to look up in the table
         * @param def Value to return if the setting is not defined.
         * @return the corresponding value, or null if not present
         */
        public static String getString(ContentResolver resolver, String name, String def) {
            String str = getStringForUser(resolver, name, UserHandle.myUserId());
            return str == null ? def : str;
        }

        /** @hide */
        public static String getStringForUser(ContentResolver resolver, String name,
                                              int userId) {
//            if (MOVED_TO_SECURE.contains(name)) {
//                Log.w(TAG, "Setting " + name + " has moved from GosuncnSettings.System"
//                        + " to GosuncnSettings.Secure, value is unchanged.");
//                return GosuncnSettings.Secure.getStringForUser(resolver, name, userId);
//            }
            return sNameValueCache.getStringForUser(resolver, name, userId);
        }

        /**
         * Store a name/value pair into the database.
         * @param resolver to access the database with
         * @param name to store
         * @param value to associate with the name
         * @return true if the value was set, false on database errors
         */
        public static boolean putString(ContentResolver resolver, String name, String value) {
            return putStringForUser(resolver, name, value, UserHandle.myUserId());
        }

        /** @hide */
        public static boolean putStringForUser(ContentResolver resolver, String name, String value,
                                               int userId) {
//            if (MOVED_TO_SECURE.contains(name)) {
//                Log.w(TAG, "Setting " + name + " has moved from GosuncnSettings.System"
//                        + " to GosuncnSettings.Secure, value is unchanged.");
//                return false;
//            }
            return sNameValueCache.putStringForUser(resolver, name, value, userId);
        }

        /**
         * Convenience function for retrieving a single settings value
         * as an integer.  Note that internally setting values are always
         * stored as strings; this function converts the string to an integer
         * for you.  The default value will be returned if the setting is
         * not defined or not an integer.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         * @param def Value to return if the setting is not defined.
         *
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid integer.
         */
        public static int getInt(ContentResolver cr, String name, int def) {
            return getIntForUser(cr, name, def, UserHandle.myUserId());
        }

        /** @hide */
        public static int getIntForUser(ContentResolver cr, String name, int def, int userId) {
            String v = getStringForUser(cr, name, userId);
            try {
                return v != null ? Integer.parseInt(v) : def;
            } catch (NumberFormatException e) {
                return def;
            }
        }

        /**
         * Convenience function for retrieving a single settings value
         * as an integer.  Note that internally setting values are always
         * stored as strings; this function converts the string to an integer
         * for you.
         * <p>
         * This version does not take a default value.  If the setting has not
         * been set, or the string value is not a number,
         * it throws {@link GosuncnSettingNotFoundException}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         *
         * @throws GosuncnSettingNotFoundException Thrown if a setting by the given
         * name can't be found or the setting value is not an integer.
         *
         * @return The setting's current value.
         */
        public static int getInt(ContentResolver cr, String name)
                throws GosuncnSettingNotFoundException {
            return getIntForUser(cr, name, UserHandle.myUserId());
        }

        /** @hide */
        public static int getIntForUser(ContentResolver cr, String name, int userId)
                throws GosuncnSettingNotFoundException {
            String v = getStringForUser(cr, name, userId);
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                throw new GosuncnSettingNotFoundException(name);
            }
        }

        /**
         * Convenience function for updating a single settings value as an
         * integer. This will either create a new entry in the table if the
         * given name does not exist, or modify the value of the existing row
         * with that name.  Note that internally setting values are always
         * stored as strings, so this function converts the given value to a
         * string before storing it.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to modify.
         * @param value The new value for the setting.
         * @return true if the value was set, false on database errors
         */
        public static boolean putInt(ContentResolver cr, String name, int value) {
            return putIntForUser(cr, name, value, UserHandle.myUserId());
        }

        /** @hide */
        public static boolean putIntForUser(ContentResolver cr, String name, int value,
                                            int userId) {
            return putStringForUser(cr, name, Integer.toString(value), userId);
        }

        /**
         * Convenience function for retrieving a single settings value
         * as a {@code long}.  Note that internally setting values are always
         * stored as strings; this function converts the string to a {@code long}
         * for you.  The default value will be returned if the setting is
         * not defined or not a {@code long}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         * @param def Value to return if the setting is not defined.
         *
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid {@code long}.
         */
        public static long getLong(ContentResolver cr, String name, long def) {
            return getLongForUser(cr, name, def, UserHandle.myUserId());
        }

        /** @hide */
        public static long getLongForUser(ContentResolver cr, String name, long def,
                                          int userId) {
            String valString = getStringForUser(cr, name, userId);
            long value;
            try {
                value = valString != null ? Long.parseLong(valString) : def;
            } catch (NumberFormatException e) {
                value = def;
            }
            return value;
        }

        /**
         * Convenience function for retrieving a single settings value
         * as a {@code long}.  Note that internally setting values are always
         * stored as strings; this function converts the string to a {@code long}
         * for you.
         * <p>
         * This version does not take a default value.  If the setting has not
         * been set, or the string value is not a number,
         * it throws {@link GosuncnSettingNotFoundException}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         *
         * @return The setting's current value.
         * @throws GosuncnSettingNotFoundException Thrown if a setting by the given
         * name can't be found or the setting value is not an integer.
         */
        public static long getLong(ContentResolver cr, String name)
                throws GosuncnSettingNotFoundException {
            return getLongForUser(cr, name, UserHandle.myUserId());
        }

        /** @hide */
        public static long getLongForUser(ContentResolver cr, String name, int userId)
                throws GosuncnSettingNotFoundException {
            String valString = getStringForUser(cr, name, userId);
            try {
                return Long.parseLong(valString);
            } catch (NumberFormatException e) {
                throw new GosuncnSettingNotFoundException(name);
            }
        }

        /**
         * Convenience function for updating a single settings value as a long
         * integer. This will either create a new entry in the table if the
         * given name does not exist, or modify the value of the existing row
         * with that name.  Note that internally setting values are always
         * stored as strings, so this function converts the given value to a
         * string before storing it.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to modify.
         * @param value The new value for the setting.
         * @return true if the value was set, false on database errors
         */
        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putLongForUser(cr, name, value, UserHandle.myUserId());
        }

        /** @hide */
        public static boolean putLongForUser(ContentResolver cr, String name, long value,
                                             int userId) {
            return putStringForUser(cr, name, Long.toString(value), userId);
        }

        /**
         * Convenience function for retrieving a single settings value
         * as a floating point number.  Note that internally setting values are
         * always stored as strings; this function converts the string to an
         * float for you. The default value will be returned if the setting
         * is not defined or not a valid float.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         * @param def Value to return if the setting is not defined.
         *
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid float.
         */
        public static float getFloat(ContentResolver cr, String name, float def) {
            return getFloatForUser(cr, name, def, UserHandle.myUserId());
        }

        /** @hide */
        public static float getFloatForUser(ContentResolver cr, String name, float def,
                                            int userId) {
            String v = getStringForUser(cr, name, userId);
            try {
                return v != null ? Float.parseFloat(v) : def;
            } catch (NumberFormatException e) {
                return def;
            }
        }

        /**
         * Convenience function for retrieving a single system settings value
         * as a float.  Note that internally setting values are always
         * stored as strings; this function converts the string to a float
         * for you.
         * <p>
         * This version does not take a default value.  If the setting has not
         * been set, or the string value is not a number,
         * it throws {@link GosuncnSettingNotFoundException}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         *
         * @throws GosuncnSettingNotFoundException Thrown if a setting by the given
         * name can't be found or the setting value is not a float.
         *
         * @return The setting's current value.
         */
        public static float getFloat(ContentResolver cr, String name)
                throws GosuncnSettingNotFoundException {
            return getFloatForUser(cr, name, UserHandle.myUserId());
        }

        /** @hide */
        public static float getFloatForUser(ContentResolver cr, String name, int userId)
                throws GosuncnSettingNotFoundException {
            String v = getStringForUser(cr, name, userId);
            if (v == null) {
                throw new GosuncnSettingNotFoundException(name);
            }
            try {
                return Float.parseFloat(v);
            } catch (NumberFormatException e) {
                throw new GosuncnSettingNotFoundException(name);
            }
        }

        /**
         * Convenience function for updating a single settings value as a
         * floating point number. This will either create a new entry in the
         * table if the given name does not exist, or modify the value of the
         * existing row with that name.  Note that internally setting values
         * are always stored as strings, so this function converts the given
         * value to a string before storing it.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to modify.
         * @param value The new value for the setting.
         * @return true if the value was set, false on database errors
         */
        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putFloatForUser(cr, name, value, UserHandle.myUserId());
        }

        /** @hide */
        public static boolean putFloatForUser(ContentResolver cr, String name, float value,
                                              int userId) {
            return putStringForUser(cr, name, Float.toString(value), userId);
        }

        // endregion

        // region System Settings
        /**
         * Whether to enable system profiles feature
         * 0 = off, 1 = on
         */
        public static final String SYSTEM_PROFILES_ENABLED = "system_profiles_enabled";

        /** @hide */
        public static final Validator SYSTEM_PROFILES_ENABLED_VALIDATOR =
                sBooleanValidator;

        /**
         * @hide
         */
        public static final String[] LEGACY_SYSTEM_SETTINGS = new String[]{
                GosuncnSettings.System.SYSTEM_PROFILES_ENABLED,
        };

        /**
         * @hide
         */
        public static boolean isLegacySetting(String key) {
            return ArrayUtils.contains(LEGACY_SYSTEM_SETTINGS, key);
        }

        /**
         * @hide
         */
        public static boolean shouldInterceptSystemProvider(String key) {
            switch (key) {
                case System.SYSTEM_PROFILES_ENABLED:
                    return true;
                default:
                    return false;
            }
        }

        /**
         * Mapping of validators for all system settings.  This map is used to validate both valid
         * keys as well as validating the values for those keys.
         *
         * Note: Make sure if you add a new System setting you create a Validator for it and add
         *       it to this map.
         *
         * @hide
         */
        public static final Map<String, Validator> VALIDATORS =
                new ArrayMap<String, Validator>();
        static {
            VALIDATORS.put(SYSTEM_PROFILES_ENABLED, SYSTEM_PROFILES_ENABLED_VALIDATOR);
        };
        // endregion
    }

    /**
     * Secure settings, containing miscellaneous Gosuncn secure preferences. This
     * table holds simple name/value pairs. There are convenience
     * functions for accessing individual settings entries.
     */
    public static final class Secure extends Settings.NameValueTable {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/secure");

        public static final String SYS_PROP_GOSUNCN_SETTING_VERSION = "sys.gosuncn_settings_secure_version";

        private static final NameValueCache sNameValueCache = new NameValueCache(
                SYS_PROP_GOSUNCN_SETTING_VERSION,
                CONTENT_URI,
                CALL_METHOD_GET_SECURE,
                CALL_METHOD_PUT_SECURE);

        /** @hide */
        protected static final ArraySet<String> MOVED_TO_GLOBAL;
        static {
            MOVED_TO_GLOBAL = new ArraySet<>(1);
        }

        // region Methods

        /**
         * Put a delimited list as a string
         * @param resolver to access the database with
         * @param name to store
         * @param delimiter to split
         * @param list to join and store
         * @hide
         */
        public static void putListAsDelimitedString(ContentResolver resolver, String name,
                                                    String delimiter, List<String> list) {
            String store = TextUtils.join(delimiter, list);
            putString(resolver, name, store);
        }

        /**
         * Get a delimited string returned as a list
         * @param resolver to access the database with
         * @param name to store
         * @param delimiter to split the list with
         * @return list of strings for a specific Settings.Secure item
         * @hide
         */
        public static List<String> getDelimitedStringAsList(ContentResolver resolver, String name,
                                                            String delimiter) {
            String baseString = getString(resolver, name);
            List<String> list = new ArrayList<String>();
            if (!TextUtils.isEmpty(baseString)) {
                final String[] array = TextUtils.split(baseString, Pattern.quote(delimiter));
                for (String item : array) {
                    if (TextUtils.isEmpty(item)) {
                        continue;
                    }
                    list.add(item);
                }
            }
            return list;
        }

        /**
         * Construct the content URI for a particular name/value pair, useful for monitoring changes
         * with a ContentObserver.
         * @param name to look up in the table
         * @return the corresponding content URI
         */
        public static Uri getUriFor(String name) {
            return Settings.NameValueTable.getUriFor(CONTENT_URI, name);
        }

        /**
         * Look up a name in the database.
         * @param resolver to access the database with
         * @param name to look up in the table
         * @return the corresponding value, or null if not present
         */
        public static String getString(ContentResolver resolver, String name) {
            return getStringForUser(resolver, name, UserHandle.myUserId());
        }

        /**
         * Look up a name in the database.
         * @param resolver to access the database with
         * @param name to look up in the table
         * @param def Value to return if the setting is not defined.
         * @return the corresponding value, or null if not present
         */
        public static String getString(ContentResolver resolver, String name, String def) {
            String str = getStringForUser(resolver, name, UserHandle.myUserId());
            return str == null ? def : str;
        }

        /** @hide */
        public static String getStringForUser(ContentResolver resolver, String name,
                                              int userId) {
            if (MOVED_TO_GLOBAL.contains(name)) {
                Log.w(TAG, "Setting " + name + " has moved from GosuncnSettings.Secure"
                        + " to GosuncnSettings.Global, value is unchanged.");
                return GosuncnSettings.Global.getStringForUser(resolver, name, userId);
            }
            return sNameValueCache.getStringForUser(resolver, name, userId);
        }

        /**
         * Store a name/value pair into the database.
         * @param resolver to access the database with
         * @param name to store
         * @param value to associate with the name
         * @return true if the value was set, false on database errors
         */
        public static boolean putString(ContentResolver resolver, String name, String value) {
            return putStringForUser(resolver, name, value, UserHandle.myUserId());
        }

        /** @hide */
        public static boolean putStringForUser(ContentResolver resolver, String name, String value,
                                               int userId) {
            if (MOVED_TO_GLOBAL.contains(name)) {
                Log.w(TAG, "Setting " + name + " has moved from GosuncnSettings.Secure"
                        + " to GosuncnSettings.Global, value is unchanged.");
                return false;
            }
            return sNameValueCache.putStringForUser(resolver, name, value, userId);
        }

        /**
         * Convenience function for retrieving a single settings value
         * as an integer.  Note that internally setting values are always
         * stored as strings; this function converts the string to an integer
         * for you.  The default value will be returned if the setting is
         * not defined or not an integer.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         * @param def Value to return if the setting is not defined.
         *
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid integer.
         */
        public static int getInt(ContentResolver cr, String name, int def) {
            return getIntForUser(cr, name, def, UserHandle.myUserId());
        }

        /** @hide */
        public static int getIntForUser(ContentResolver cr, String name, int def, int userId) {
            String v = getStringForUser(cr, name, userId);
            try {
                return v != null ? Integer.parseInt(v) : def;
            } catch (NumberFormatException e) {
                return def;
            }
        }

        /**
         * Convenience function for retrieving a single settings value
         * as an integer.  Note that internally setting values are always
         * stored as strings; this function converts the string to an integer
         * for you.
         * <p>
         * This version does not take a default value.  If the setting has not
         * been set, or the string value is not a number,
         * it throws {@link GosuncnSettingNotFoundException}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         *
         * @throws GosuncnSettingNotFoundException Thrown if a setting by the given
         * name can't be found or the setting value is not an integer.
         *
         * @return The setting's current value.
         */
        public static int getInt(ContentResolver cr, String name)
                throws GosuncnSettingNotFoundException {
            return getIntForUser(cr, name, UserHandle.myUserId());
        }

        /** @hide */
        public static int getIntForUser(ContentResolver cr, String name, int userId)
                throws GosuncnSettingNotFoundException {
            String v = getStringForUser(cr, name, userId);
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                throw new GosuncnSettingNotFoundException(name);
            }
        }

        /**
         * Convenience function for updating a single settings value as an
         * integer. This will either create a new entry in the table if the
         * given name does not exist, or modify the value of the existing row
         * with that name.  Note that internally setting values are always
         * stored as strings, so this function converts the given value to a
         * string before storing it.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to modify.
         * @param value The new value for the setting.
         * @return true if the value was set, false on database errors
         */
        public static boolean putInt(ContentResolver cr, String name, int value) {
            return putIntForUser(cr, name, value, UserHandle.myUserId());
        }

        /** @hide */
        public static boolean putIntForUser(ContentResolver cr, String name, int value,
                                            int userId) {
            return putStringForUser(cr, name, Integer.toString(value), userId);
        }

        /**
         * Convenience function for retrieving a single settings value
         * as a {@code long}.  Note that internally setting values are always
         * stored as strings; this function converts the string to a {@code long}
         * for you.  The default value will be returned if the setting is
         * not defined or not a {@code long}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         * @param def Value to return if the setting is not defined.
         *
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid {@code long}.
         */
        public static long getLong(ContentResolver cr, String name, long def) {
            return getLongForUser(cr, name, def, UserHandle.myUserId());
        }

        /** @hide */
        public static long getLongForUser(ContentResolver cr, String name, long def,
                                          int userId) {
            String valString = getStringForUser(cr, name, userId);
            long value;
            try {
                value = valString != null ? Long.parseLong(valString) : def;
            } catch (NumberFormatException e) {
                value = def;
            }
            return value;
        }

        /**
         * Convenience function for retrieving a single settings value
         * as a {@code long}.  Note that internally setting values are always
         * stored as strings; this function converts the string to a {@code long}
         * for you.
         * <p>
         * This version does not take a default value.  If the setting has not
         * been set, or the string value is not a number,
         * it throws {@link GosuncnSettingNotFoundException}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         *
         * @return The setting's current value.
         * @throws GosuncnSettingNotFoundException Thrown if a setting by the given
         * name can't be found or the setting value is not an integer.
         */
        public static long getLong(ContentResolver cr, String name)
                throws GosuncnSettingNotFoundException {
            return getLongForUser(cr, name, UserHandle.myUserId());
        }

        /** @hide */
        public static long getLongForUser(ContentResolver cr, String name, int userId)
                throws GosuncnSettingNotFoundException {
            String valString = getStringForUser(cr, name, userId);
            try {
                return Long.parseLong(valString);
            } catch (NumberFormatException e) {
                throw new GosuncnSettingNotFoundException(name);
            }
        }

        /**
         * Convenience function for updating a single settings value as a long
         * integer. This will either create a new entry in the table if the
         * given name does not exist, or modify the value of the existing row
         * with that name.  Note that internally setting values are always
         * stored as strings, so this function converts the given value to a
         * string before storing it.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to modify.
         * @param value The new value for the setting.
         * @return true if the value was set, false on database errors
         */
        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putLongForUser(cr, name, value, UserHandle.myUserId());
        }

        /** @hide */
        public static boolean putLongForUser(ContentResolver cr, String name, long value,
                                             int userId) {
            return putStringForUser(cr, name, Long.toString(value), userId);
        }

        /**
         * Convenience function for retrieving a single settings value
         * as a floating point number.  Note that internally setting values are
         * always stored as strings; this function converts the string to an
         * float for you. The default value will be returned if the setting
         * is not defined or not a valid float.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         * @param def Value to return if the setting is not defined.
         *
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid float.
         */
        public static float getFloat(ContentResolver cr, String name, float def) {
            return getFloatForUser(cr, name, def, UserHandle.myUserId());
        }

        /** @hide */
        public static float getFloatForUser(ContentResolver cr, String name, float def,
                                            int userId) {
            String v = getStringForUser(cr, name, userId);
            try {
                return v != null ? Float.parseFloat(v) : def;
            } catch (NumberFormatException e) {
                return def;
            }
        }

        /**
         * Convenience function for retrieving a single system settings value
         * as a float.  Note that internally setting values are always
         * stored as strings; this function converts the string to a float
         * for you.
         * <p>
         * This version does not take a default value.  If the setting has not
         * been set, or the string value is not a number,
         * it throws {@link GosuncnSettingNotFoundException}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         *
         * @throws GosuncnSettingNotFoundException Thrown if a setting by the given
         * name can't be found or the setting value is not a float.
         *
         * @return The setting's current value.
         */
        public static float getFloat(ContentResolver cr, String name)
                throws GosuncnSettingNotFoundException {
            return getFloatForUser(cr, name, UserHandle.myUserId());
        }

        /** @hide */
        public static float getFloatForUser(ContentResolver cr, String name, int userId)
                throws GosuncnSettingNotFoundException {
            String v = getStringForUser(cr, name, userId);
            if (v == null) {
                throw new GosuncnSettingNotFoundException(name);
            }
            try {
                return Float.parseFloat(v);
            } catch (NumberFormatException e) {
                throw new GosuncnSettingNotFoundException(name);
            }
        }

        /**
         * Convenience function for updating a single settings value as a
         * floating point number. This will either create a new entry in the
         * table if the given name does not exist, or modify the value of the
         * existing row with that name.  Note that internally setting values
         * are always stored as strings, so this function converts the given
         * value to a string before storing it.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to modify.
         * @param value The new value for the setting.
         * @return true if the value was set, false on database errors
         */
        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putFloatForUser(cr, name, value, UserHandle.myUserId());
        }

        /** @hide */
        public static boolean putFloatForUser(ContentResolver cr, String name, float value,
                                              int userId) {
            return putStringForUser(cr, name, Float.toString(value), userId);
        }

        // endregion

        // region Secure Settings
        /**
         * Whether to enable "advanced mode" for the current user.
         * Boolean setting. 0 = no, 1 = yes.
         * @hide
         */
        public static final String ADVANCED_MODE = "advanced_mode";

        // endregion

        /**
         * @hide
         */
        public static final String[] LEGACY_SECURE_SETTINGS = new String[]{
                GosuncnSettings.Secure.ADVANCED_MODE,

        };

        /**
         * @hide
         */
        public static boolean isLegacySetting(String key) {
            return ArrayUtils.contains(LEGACY_SECURE_SETTINGS, key);
        }

        /** Protected Components
         * @hide
         */
        public static final String PROTECTED_COMPONENTS = "protected_components";

        /**
         * @hide
         */
        public static final Validator PROTECTED_COMPONENTS_VALIDATOR = new Validator() {
            private final String mDelimiter = "|";

            @Override
            public boolean validate(String value) {
                if (!TextUtils.isEmpty(value)) {
                    final String[] array = TextUtils.split(value, Pattern.quote(mDelimiter));
                    for (String item : array) {
                        if (TextUtils.isEmpty(item)) {
                            return false; // Empty components not allowed
                        }
                    }
                }
                return true;  // Empty list is allowed though.
            }
        };

        /**
         * Mapping of validators for all secure settings.  This map is used to validate both valid
         * keys as well as validating the values for those keys.
         *
         * Note: Make sure if you add a new Secure setting you create a Validator for it and add
         *       it to this map.
         *
         * @hide
         */
        public static final Map<String, Validator> VALIDATORS =
                new ArrayMap<String, Validator>();
        static {
            VALIDATORS.put(PROTECTED_COMPONENTS, PROTECTED_COMPONENTS_VALIDATOR);
        }

        /**
         * @hide
         */
        public static boolean shouldInterceptSystemProvider(String key) {
            switch (key) {
                default:
                    return false;
            }
        }
    }

    /**
     * Global settings, containing miscellaneous Gosuncn global preferences. This
     * table holds simple name/value pairs. There are convenience
     * functions for accessing individual settings entries.
     */
    public static final class Global extends Settings.NameValueTable {
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/global");

        public static final String SYS_PROP_GOSUNCN_SETTING_VERSION = "sys.gosuncn_settings_global_version";

        private static final NameValueCache sNameValueCache = new NameValueCache(
                SYS_PROP_GOSUNCN_SETTING_VERSION,
                CONTENT_URI,
                CALL_METHOD_GET_GLOBAL,
                CALL_METHOD_PUT_GLOBAL);

        // region Methods


        /**
         * Put a delimited list as a string
         * @param resolver to access the database with
         * @param name to store
         * @param delimiter to split
         * @param list to join and store
         * @hide
         */
        public static void putListAsDelimitedString(ContentResolver resolver, String name,
                                                    String delimiter, List<String> list) {
            String store = TextUtils.join(delimiter, list);
            putString(resolver, name, store);
        }

        /**
         * Get a delimited string returned as a list
         * @param resolver to access the database with
         * @param name to store
         * @param delimiter to split the list with
         * @return list of strings for a specific Settings.Secure item
         * @hide
         */
        public static List<String> getDelimitedStringAsList(ContentResolver resolver, String name,
                                                            String delimiter) {
            String baseString = getString(resolver, name);
            List<String> list = new ArrayList<String>();
            if (!TextUtils.isEmpty(baseString)) {
                final String[] array = TextUtils.split(baseString, Pattern.quote(delimiter));
                for (String item : array) {
                    if (TextUtils.isEmpty(item)) {
                        continue;
                    }
                    list.add(item);
                }
            }
            return list;
        }

        /**
         * Construct the content URI for a particular name/value pair, useful for monitoring changes
         * with a ContentObserver.
         * @param name to look up in the table
         * @return the corresponding content URI
         */
        public static Uri getUriFor(String name) {
            return Settings.NameValueTable.getUriFor(CONTENT_URI, name);
        }

        /**
         * Look up a name in the database.
         * @param resolver to access the database with
         * @param name to look up in the table
         * @return the corresponding value, or null if not present
         */
        public static String getString(ContentResolver resolver, String name) {
            return getStringForUser(resolver, name, UserHandle.myUserId());
        }

        /**
         * Look up a name in the database.
         * @param resolver to access the database with
         * @param name to look up in the table
         * @param def Value to return if the setting is not defined.
         * @return the corresponding value, or null if not present
         */
        public static String getString(ContentResolver resolver, String name, String def) {
            String str = getStringForUser(resolver, name, UserHandle.myUserId());
            return str == null ? def : str;
        }

        /** @hide */
        public static String getStringForUser(ContentResolver resolver, String name,
                                              int userId) {
            return sNameValueCache.getStringForUser(resolver, name, userId);
        }

        /**
         * Store a name/value pair into the database.
         * @param resolver to access the database with
         * @param name to store
         * @param value to associate with the name
         * @return true if the value was set, false on database errors
         */
        public static boolean putString(ContentResolver resolver, String name, String value) {
            return putStringForUser(resolver, name, value, UserHandle.myUserId());
        }

        /** @hide */
        public static boolean putStringForUser(ContentResolver resolver, String name, String value,
                                               int userId) {
            return sNameValueCache.putStringForUser(resolver, name, value, userId);
        }

        /**
         * Convenience function for retrieving a single settings value
         * as an integer.  Note that internally setting values are always
         * stored as strings; this function converts the string to an integer
         * for you.  The default value will be returned if the setting is
         * not defined or not an integer.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         * @param def Value to return if the setting is not defined.
         *
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid integer.
         */
        public static int getInt(ContentResolver cr, String name, int def) {
            return getIntForUser(cr, name, def, UserHandle.myUserId());
        }

        /** @hide */
        public static int getIntForUser(ContentResolver cr, String name, int def, int userId) {
            String v = getStringForUser(cr, name, userId);
            try {
                return v != null ? Integer.parseInt(v) : def;
            } catch (NumberFormatException e) {
                return def;
            }
        }

        /**
         * Convenience function for retrieving a single settings value
         * as an integer.  Note that internally setting values are always
         * stored as strings; this function converts the string to an integer
         * for you.
         * <p>
         * This version does not take a default value.  If the setting has not
         * been set, or the string value is not a number,
         * it throws {@link GosuncnSettingNotFoundException}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         *
         * @throws GosuncnSettingNotFoundException Thrown if a setting by the given
         * name can't be found or the setting value is not an integer.
         *
         * @return The setting's current value.
         */
        public static int getInt(ContentResolver cr, String name)
                throws GosuncnSettingNotFoundException {
            return getIntForUser(cr, name, UserHandle.myUserId());
        }

        /** @hide */
        public static int getIntForUser(ContentResolver cr, String name, int userId)
                throws GosuncnSettingNotFoundException {
            String v = getStringForUser(cr, name, userId);
            try {
                return Integer.parseInt(v);
            } catch (NumberFormatException e) {
                throw new GosuncnSettingNotFoundException(name);
            }
        }

        /**
         * Convenience function for updating a single settings value as an
         * integer. This will either create a new entry in the table if the
         * given name does not exist, or modify the value of the existing row
         * with that name.  Note that internally setting values are always
         * stored as strings, so this function converts the given value to a
         * string before storing it.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to modify.
         * @param value The new value for the setting.
         * @return true if the value was set, false on database errors
         */
        public static boolean putInt(ContentResolver cr, String name, int value) {
            return putIntForUser(cr, name, value, UserHandle.myUserId());
        }

        /** @hide */
        public static boolean putIntForUser(ContentResolver cr, String name, int value,
                                            int userId) {
            return putStringForUser(cr, name, Integer.toString(value), userId);
        }

        /**
         * Convenience function for retrieving a single settings value
         * as a {@code long}.  Note that internally setting values are always
         * stored as strings; this function converts the string to a {@code long}
         * for you.  The default value will be returned if the setting is
         * not defined or not a {@code long}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         * @param def Value to return if the setting is not defined.
         *
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid {@code long}.
         */
        public static long getLong(ContentResolver cr, String name, long def) {
            return getLongForUser(cr, name, def, UserHandle.myUserId());
        }

        /** @hide */
        public static long getLongForUser(ContentResolver cr, String name, long def,
                                          int userId) {
            String valString = getStringForUser(cr, name, userId);
            long value;
            try {
                value = valString != null ? Long.parseLong(valString) : def;
            } catch (NumberFormatException e) {
                value = def;
            }
            return value;
        }

        /**
         * Convenience function for retrieving a single settings value
         * as a {@code long}.  Note that internally setting values are always
         * stored as strings; this function converts the string to a {@code long}
         * for you.
         * <p>
         * This version does not take a default value.  If the setting has not
         * been set, or the string value is not a number,
         * it throws {@link GosuncnSettingNotFoundException}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         *
         * @return The setting's current value.
         * @throws GosuncnSettingNotFoundException Thrown if a setting by the given
         * name can't be found or the setting value is not an integer.
         */
        public static long getLong(ContentResolver cr, String name)
                throws GosuncnSettingNotFoundException {
            return getLongForUser(cr, name, UserHandle.myUserId());
        }

        /** @hide */
        public static long getLongForUser(ContentResolver cr, String name, int userId)
                throws GosuncnSettingNotFoundException {
            String valString = getStringForUser(cr, name, userId);
            try {
                return Long.parseLong(valString);
            } catch (NumberFormatException e) {
                throw new GosuncnSettingNotFoundException(name);
            }
        }

        /**
         * Convenience function for updating a single settings value as a long
         * integer. This will either create a new entry in the table if the
         * given name does not exist, or modify the value of the existing row
         * with that name.  Note that internally setting values are always
         * stored as strings, so this function converts the given value to a
         * string before storing it.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to modify.
         * @param value The new value for the setting.
         * @return true if the value was set, false on database errors
         */
        public static boolean putLong(ContentResolver cr, String name, long value) {
            return putLongForUser(cr, name, value, UserHandle.myUserId());
        }

        /** @hide */
        public static boolean putLongForUser(ContentResolver cr, String name, long value,
                                             int userId) {
            return putStringForUser(cr, name, Long.toString(value), userId);
        }

        /**
         * Convenience function for retrieving a single settings value
         * as a floating point number.  Note that internally setting values are
         * always stored as strings; this function converts the string to an
         * float for you. The default value will be returned if the setting
         * is not defined or not a valid float.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         * @param def Value to return if the setting is not defined.
         *
         * @return The setting's current value, or 'def' if it is not defined
         * or not a valid float.
         */
        public static float getFloat(ContentResolver cr, String name, float def) {
            return getFloatForUser(cr, name, def, UserHandle.myUserId());
        }

        /** @hide */
        public static float getFloatForUser(ContentResolver cr, String name, float def,
                                            int userId) {
            String v = getStringForUser(cr, name, userId);
            try {
                return v != null ? Float.parseFloat(v) : def;
            } catch (NumberFormatException e) {
                return def;
            }
        }

        /**
         * Convenience function for retrieving a single system settings value
         * as a float.  Note that internally setting values are always
         * stored as strings; this function converts the string to a float
         * for you.
         * <p>
         * This version does not take a default value.  If the setting has not
         * been set, or the string value is not a number,
         * it throws {@link GosuncnSettingNotFoundException}.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to retrieve.
         *
         * @throws GosuncnSettingNotFoundException Thrown if a setting by the given
         * name can't be found or the setting value is not a float.
         *
         * @return The setting's current value.
         */
        public static float getFloat(ContentResolver cr, String name)
                throws GosuncnSettingNotFoundException {
            return getFloatForUser(cr, name, UserHandle.myUserId());
        }

        /** @hide */
        public static float getFloatForUser(ContentResolver cr, String name, int userId)
                throws GosuncnSettingNotFoundException {
            String v = getStringForUser(cr, name, userId);
            if (v == null) {
                throw new GosuncnSettingNotFoundException(name);
            }
            try {
                return Float.parseFloat(v);
            } catch (NumberFormatException e) {
                throw new GosuncnSettingNotFoundException(name);
            }
        }

        /**
         * Convenience function for updating a single settings value as a
         * floating point number. This will either create a new entry in the
         * table if the given name does not exist, or modify the value of the
         * existing row with that name.  Note that internally setting values
         * are always stored as strings, so this function converts the given
         * value to a string before storing it.
         *
         * @param cr The ContentResolver to access.
         * @param name The name of the setting to modify.
         * @param value The new value for the setting.
         * @return true if the value was set, false on database errors
         */
        public static boolean putFloat(ContentResolver cr, String name, float value) {
            return putFloatForUser(cr, name, value, UserHandle.myUserId());
        }

        /** @hide */
        public static boolean putFloatForUser(ContentResolver cr, String name, float value,
                                              int userId) {
            return putStringForUser(cr, name, Float.toString(value), userId);
        }

        // endregion

        // region Global Settings
        /**
         * Whether the system auto-configure the priority of the wifi ap's or use
         * the manual settings established by the user.
         * <> 0 to autoconfigure, 0 to manual settings. Default is <> 0.
         * @hide
         */
        public static final String WIFI_AUTO_PRIORITIES_CONFIGURATION = "wifi_auto_priority";

        /**
         * @hide
         */
        public static final String[] LEGACY_GLOBAL_SETTINGS = new String[]{
                GosuncnSettings.Global.WIFI_AUTO_PRIORITIES_CONFIGURATION
        };

        /**
         * @hide
         */
        public static boolean isLegacySetting(String key) {
            return ArrayUtils.contains(LEGACY_GLOBAL_SETTINGS, key);
        }

        /**
         * @hide
         */
        public static boolean shouldInterceptSystemProvider(String key) {
            return false;
        }

        public static final String GXX_WLAN_CONFIGURATION = "gxx_wlan_configuration";

    }
}
