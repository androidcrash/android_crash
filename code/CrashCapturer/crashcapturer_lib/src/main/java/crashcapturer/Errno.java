// Copyright (c) 2019-present, iQIYI, Inc. All rights reserved.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files (the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all
// copies or substantial portions of the Software.
//

package crashcapturer;

/**
 * The errno code for init() method of {@link xcrash.XCrash}.
 */
@SuppressWarnings("WeakerAccess")
public final class Errno {

    /**
     * Initialization successful.
     */
    public static final int OK = 0;

    /**
     * The context parameter is null.
     */
    public static final int CONTEXT_IS_NULL = -1;

    /**
     * Load xCrash's native library failed.
     */
    public static final int LOAD_LIBRARY_FAILED = -2;

    /**
     * Initialize xCrash's native library failed.
     */
    public static final int INIT_LIBRARY_FAILED = -3;

    private Errno() {
    }
}
