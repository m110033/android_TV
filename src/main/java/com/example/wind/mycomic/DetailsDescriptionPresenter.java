/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.wind.mycomic;

import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;

import com.example.wind.mycomic.object.Movie;
import com.example.wind.mycomic.utils.PlayMovie;

public class DetailsDescriptionPresenter extends AbstractDetailsDescriptionPresenter {

    @Override
    protected void onBindDescription(ViewHolder viewHolder, Object item) {
        if (item instanceof Movie) {
            Movie movie = (Movie) item;
            viewHolder.getTitle().setText(movie.getTitle());
            viewHolder.getSubtitle().setText(movie.getStudio());
            viewHolder.getBody().setText(movie.getDescription());
        } else if (item instanceof PlayMovie) {
            PlayMovie movie = (PlayMovie) item;
            viewHolder.getTitle().setText(movie.getMovie_title());
            viewHolder.getSubtitle().setText(movie.getVideo_title());
            viewHolder.getBody().setText(movie.getVideo_intro());
        }
    }
}
