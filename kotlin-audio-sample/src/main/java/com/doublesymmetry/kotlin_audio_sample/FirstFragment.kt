package com.doublesymmetry.kotlin_audio_sample

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.doublesymmetry.kotlin_audio_sample.databinding.FragmentFirstBinding
import com.doublesymmetry.kotlin_audio_sample.utils.firstItem
import com.doublesymmetry.kotlin_audio_sample.utils.secondItem
import com.doublesymmetry.kotlinaudio.models.AudioPlayerState.PLAYING
import com.doublesymmetry.kotlinaudio.players.QueuedAudioPlayer
import com.orhanobut.logger.Logger
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {
    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val player = QueuedAudioPlayer(requireActivity())
        player.add(firstItem)
        player.add(secondItem)
        player.play()

        lifecycleScope.launch {
            player.event.stateChange.collect {
                Logger.d(it)

                when (it) {
                    PLAYING -> {
                        binding.buttonPlay.isEnabled = false
                        binding.buttonPause.isEnabled = true
                    }
                    else -> {
                        binding.buttonPlay.isEnabled = true
                        binding.buttonPause.isEnabled = false
                    }
                }
            }
        }

        lifecycleScope.launch {
            player.event.audioItemTransition.collect {
                binding.textviewTitle.text = player.currentItem?.title
                binding.textviewArtist.text = player.currentItem?.artist
                binding.textviewQueue.text = "${player.currentIndex + 1} / ${player.items.size}"
            }
        }

        binding.buttonNext.setOnClickListener {
            player.next()
        }

        binding.buttonPrevious.setOnClickListener {
            player.previous()
        }

        binding.buttonPlay.setOnClickListener {
            player.play()
        }

        binding.buttonPause.setOnClickListener {
            player.pause()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}