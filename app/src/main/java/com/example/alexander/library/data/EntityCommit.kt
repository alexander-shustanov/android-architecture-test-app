package com.example.alexander.library.data

class EntityCommit<T : Entity>(entity: T) {
    val entity: T = entity.copy() as T


}