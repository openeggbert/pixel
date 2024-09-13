///////////////////////////////////////////////////////////////////////////////////////////////
// Pixel: Game library.
// Copyright (C) 2024 the original author or authors.
//
// This program is free software: you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation, either version 3
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program. If not, see
// <https://www.gnu.org/licenses/> or write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
///////////////////////////////////////////////////////////////////////////////////////////////
package com.pixelgamelibrary.api.storage;

import com.pixelgamelibrary.api.PixelException;

/**
 * StorageException is a custom exception class that extends {@link PixelException}.
 * It represents exceptions that occur within the storage system of the Pixel Game Library.
 * 
 * @author robertvokac
 */
public class StorageException extends PixelException {

    /**
     * Constructs a new StorageException with the specified detail message.
     * 
     * @param string the detail message for this exception.
     */
    public StorageException(String string) {
        super(string);
    }
    
}
