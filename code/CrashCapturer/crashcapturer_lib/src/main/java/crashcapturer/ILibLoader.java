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
 * Define the native library loader.
 *
 * <p>In practice, older versions of Android had bugs in PackageManager
 * that caused installation and update of native libraries to be unreliable.
 */
public interface ILibLoader {

    /**
     * Loads the native library specified by the libName argument.
     *
     * @param libName the name of the library.
     */
    void loadLibrary(String libName);
}
