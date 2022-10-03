package com.karrar.movieapp.ui.home.adapters

import com.karrar.movieapp.R
import com.karrar.movieapp.domain.models.Actor
import com.karrar.movieapp.domain.models.PopularMovie
import com.karrar.movieapp.ui.base.BaseAdapter
import com.karrar.movieapp.ui.base.BaseInteractionListener


class ActorAdapter(items: List<Actor>, listener: HomeInteractionListener) :
    BaseAdapter<Actor>(items, listener) {
    override var layoutID: Int = R.layout.item_actor
}

